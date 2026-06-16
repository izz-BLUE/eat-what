package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.EatRecordStatus;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.request.CompleteRecordRequest;
import com.fantuan.eatwhat.dto.request.DecideRecordRequest;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
import com.fantuan.eatwhat.dto.request.ReviewRecordRequest;
import com.fantuan.eatwhat.dto.response.EatRecordResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.EatRecordMapper;
import com.fantuan.eatwhat.mapper.FoodMapper;
import com.fantuan.eatwhat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 吃过记录服务
 *
 * 并发安全策略（双层锁）：
 * 1. 应用层按 userId 的 ReentrantLock — 保证同用户 DECIDED 生命周期操作串行，
 *    不依赖数据库 FOR UPDATE 的实现差异（H2 vs MySQL）。
 * 2. 数据库 SELECT ... FOR UPDATE — MySQL 环境下的二级保护，保留不动。
 *
 * 受保护方法：createDecision / createRecord / completeRecord / cancelDecision
 */
@Service
@RequiredArgsConstructor
public class EatRecordService {

    private final EatRecordMapper eatRecordMapper;
    private final FoodMapper foodMapper;
    private final UserMapper userMapper;

    /** 自注入，用于从应用锁内部调用 @Transactional 方法穿透 AOP 代理 */
    @Lazy
    @Autowired
    private EatRecordService self;

    /** 按 userId 的串行锁，仅包住 DECIDED 生命周期变更 */
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    /**
     * 返回可穿透 @Transactional 代理的自身引用。
     * 在 Spring 容器中返回代理 self；在 Mockito 测试中（self==null）回退到 this。
     */
    private EatRecordService tx() {
        return self != null ? self : this;
    }

    /**
     * 在用户锁内执行操作。
     * 锁在 Supplier 返回后才释放，确保 @Transactional 方法的事务在锁覆盖范围内提交。
     */
    private <T> T withUserLock(Long userId, Supplier<T> action) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    // ==================== 旧接口（兼容） ====================

    /**
     * 创建吃过记录（直接标记为 EATEN，保留旧接口兼容）
     *
     * 先获取用户锁，再在事务内清理已有 DECIDED，最后创建 EATEN。
     * 避免与 decide 并发时留下孤儿 DECIDED。
     */
    public EatRecordResponse createRecord(Long userId, EatRecordRequest request) {
        return withUserLock(userId, () -> tx().createRecordInternal(userId, request));
    }

    @Transactional
    EatRecordResponse createRecordInternal(Long userId, EatRecordRequest request) {
        Food food = foodMapper.selectById(request.getFoodId());
        if (food == null || !Boolean.TRUE.equals(food.getEnabled())) {
            throw new BusinessException(ResultCode.FOOD_NOT_FOUND);
        }

        // 锁定用户行，与 decide/complete/cancel 互斥
        Long lockedUserId = userMapper.selectUserIdForUpdate(userId);
        if (lockedUserId == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 清理用户已有的 DECIDED 记录
        LambdaQueryWrapper<EatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EatRecord::getUserId, userId)
                .eq(EatRecord::getStatus, EatRecordStatus.DECIDED);
        List<EatRecord> decidedRecords = eatRecordMapper.selectList(wrapper);
        for (EatRecord decided : decidedRecords) {
            eatRecordMapper.deleteById(decided.getId());
        }

        EatRecord record = new EatRecord();
        record.setUserId(userId);
        record.setFoodId(request.getFoodId());
        record.setMealType(request.getMealType());
        record.setStatus(EatRecordStatus.EATEN);
        record.setEatenAt(LocalDateTime.now());
        record.setRating(request.getRating());
        record.setNote(request.getNote());

        eatRecordMapper.insert(record);

        return buildResponse(record, food.getName(), food.getCategory());
    }

    // ==================== 新接口 ====================

    /**
     * 决定吃什么（创建 DECIDED 记录）
     *
     * 应用层用户锁 + 数据库行锁双重保护。
     *
     * 幂等规则：
     * - 同 foodId + 同 mealType → 返回原记录（真幂等）
     * - 同 foodId + 不同 mealType → 更新原记录 mealType 后返回
     * - 不同 foodId → 删除旧 DECIDED，创建新 DECIDED
     */
    public EatRecordResponse createDecision(Long userId, DecideRecordRequest request) {
        return withUserLock(userId, () -> tx().createDecisionInternal(userId, request));
    }

    @Transactional
    EatRecordResponse createDecisionInternal(Long userId, DecideRecordRequest request) {
        // 1. 校验食物存在且启用
        Food food = foodMapper.selectById(request.getFoodId());
        if (food == null || !Boolean.TRUE.equals(food.getEnabled())) {
            throw new BusinessException(ResultCode.FOOD_NOT_FOUND);
        }

        // 2. 锁定用户行（用户行一定存在，确保并发互斥）
        Long lockedUserId = userMapper.selectUserIdForUpdate(userId);
        if (lockedUserId == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 查询用户当前 DECIDED 记录（用户锁已持有）
        LambdaQueryWrapper<EatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EatRecord::getUserId, userId)
                .eq(EatRecord::getStatus, EatRecordStatus.DECIDED);
        EatRecord existingDecided = eatRecordMapper.selectOne(wrapper);

        // 4. 同 foodId → 幂等处理
        if (existingDecided != null && existingDecided.getFoodId().equals(request.getFoodId())) {
            if (!request.getMealType().equals(existingDecided.getMealType())) {
                // mealType 不同 → 更新原记录
                existingDecided.setMealType(request.getMealType());
                eatRecordMapper.updateById(existingDecided);
            }
            return buildResponse(existingDecided, food.getName(), food.getCategory());
        }

        // 5. 不同 foodId → 删除旧 DECIDED
        if (existingDecided != null) {
            eatRecordMapper.deleteById(existingDecided.getId());
        }

        // 6. 创建新 DECIDED 记录
        EatRecord record = new EatRecord();
        record.setUserId(userId);
        record.setFoodId(request.getFoodId());
        record.setMealType(request.getMealType());
        record.setStatus(EatRecordStatus.DECIDED);
        record.setDecidedAt(LocalDateTime.now());

        eatRecordMapper.insert(record);

        return buildResponse(record, food.getName(), food.getCategory());
    }

    /**
     * 完成用餐（DECIDED → EATEN）
     *
     * 应用层用户锁 + 数据库行锁双重保护。
     * 检查 updateById 影响行数。
     */
    public EatRecordResponse completeRecord(Long userId, Long recordId, CompleteRecordRequest request) {
        return withUserLock(userId, () -> tx().completeRecordInternal(userId, recordId, request));
    }

    @Transactional
    EatRecordResponse completeRecordInternal(Long userId, Long recordId, CompleteRecordRequest request) {
        // 锁定用户行，与 decide/cancel/eat 互斥
        Long lockedUserId = userMapper.selectUserIdForUpdate(userId);
        if (lockedUserId == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        EatRecord record = findRecordOrFail(recordId, userId);

        if (!EatRecordStatus.DECIDED.equals(record.getStatus())) {
            throw new BusinessException(ResultCode.RECORD_STATUS_INVALID, "只能完成已决定的记录");
        }

        record.setStatus(EatRecordStatus.EATEN);
        record.setEatenAt(LocalDateTime.now());
        record.setRating(request.getRating());
        record.setNote(request.getNote());

        int updated = eatRecordMapper.updateById(record);
        if (updated == 0) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }

        Food food = foodMapper.selectById(record.getFoodId());
        return buildResponse(record, food != null ? food.getName() : "未知",
                food != null ? food.getCategory() : "");
    }

    /**
     * 修改已吃记录的评价
     */
    public EatRecordResponse reviewRecord(Long userId, Long recordId, ReviewRecordRequest request) {
        EatRecord record = findRecordOrFail(recordId, userId);

        if (!EatRecordStatus.EATEN.equals(record.getStatus())) {
            throw new BusinessException(ResultCode.RECORD_STATUS_INVALID, "只能评价已吃的记录");
        }

        if (request.getRating() != null) {
            record.setRating(request.getRating());
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }

        int updated = eatRecordMapper.updateById(record);
        if (updated == 0) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }

        Food food = foodMapper.selectById(record.getFoodId());
        return buildResponse(record, food != null ? food.getName() : "未知",
                food != null ? food.getCategory() : "");
    }

    /**
     * 取消决定（删除 DECIDED 记录）
     *
     * 应用层用户锁 + 数据库行锁双重保护。
     * 检查 deleteById 影响行数。
     */
    public void cancelDecision(Long userId, Long recordId) {
        withUserLock(userId, () -> {
            tx().cancelDecisionInternal(userId, recordId);
            return null;
        });
    }

    @Transactional
    void cancelDecisionInternal(Long userId, Long recordId) {
        // 锁定用户行，与 decide/complete/eat 互斥
        Long lockedUserId = userMapper.selectUserIdForUpdate(userId);
        if (lockedUserId == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        EatRecord record = findRecordOrFail(recordId, userId);

        if (!EatRecordStatus.DECIDED.equals(record.getStatus())) {
            throw new BusinessException(ResultCode.RECORD_STATUS_INVALID, "只能取消已决定的记录");
        }

        int deleted = eatRecordMapper.deleteById(recordId);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
    }

    /**
     * 获取单条记录详情（含食物名、分类），校验归属
     */
    public EatRecordResponse getRecord(Long userId, Long recordId) {
        EatRecord record = findRecordOrFail(recordId, userId);

        Food food = foodMapper.selectById(record.getFoodId());
        return buildResponse(record, food != null ? food.getName() : "未知",
                food != null ? food.getCategory() : "");
    }

    // ==================== 列表查询 ====================

    /**
     * 查询用户用餐记录
     *
     * 排序：DECIDED 排最前，然后按 COALESCE(eaten_at, decided_at) DESC
     */
    public List<EatRecordResponse> listRecords(Long userId, int limit) {
        LambdaQueryWrapper<EatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EatRecord::getUserId, userId)
                .orderByAsc(EatRecord::getStatus)   // DECIDED < EATEN 字母序，DECIDED 在前
                .orderByDesc(EatRecord::getEatenAt) // EATEN 按吃的时间倒序
                .last("LIMIT " + limit);

        List<EatRecord> records = eatRecordMapper.selectList(wrapper);

        // 批量查询食物名称
        List<Long> foodIds = records.stream()
                .map(EatRecord::getFoodId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, Food> foodMap;
        if (!foodIds.isEmpty()) {
            List<Food> foods = foodMapper.selectBatchIds(foodIds);
            foodMap = foods.stream()
                    .collect(Collectors.toMap(Food::getId, f -> f));
        } else {
            foodMap = Map.of();
        }

        return records.stream()
                .map(record -> {
                    Food food = foodMap.get(record.getFoodId());
                    return buildResponse(record,
                            food != null ? food.getName() : "未知",
                            food != null ? food.getCategory() : "");
                })
                .collect(Collectors.toList());
    }

    // ==================== 推荐辅助 ====================

    /**
     * 查询用户有评分的已吃记录（仅 EATEN 状态，rating 非空）
     *
     * 用于推荐算法的评分偏好加权：高分记录的标签给候选菜加分，
     * 低分记录的标签给候选菜降权。
     *
     * @param userId 用户ID，null 时返回空列表
     * @return 有评分的 EATEN 记录列表
     */
    public List<EatRecord> getRatedEatenRecords(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return eatRecordMapper.selectRatedEatenRecords(userId);
    }

    /**
     * 查询用户最近7天吃过的食物及其最近吃的时间（仅 EATEN 状态）
     *
     * @param userId 用户ID
     * @return 食物ID → 最近吃的时间
     */
    public Map<Long, LocalDateTime> getRecentEatenFoodMap(Long userId) {
        if (userId == null) {
            return Map.of();
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<Map<String, Object>> results = eatRecordMapper.selectRecentEatenFoods(userId, sevenDaysAgo);

        Map<Long, LocalDateTime> map = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long foodId = ((Number) row.get("foodId")).longValue();
            LocalDateTime lastEatenAt = (LocalDateTime) row.get("lastEatenAt");
            map.put(foodId, lastEatenAt);
        }
        return map;
    }

    // ==================== 私有方法 ====================

    /**
     * 按 id + userId 查询记录，不存在统一返回 RECORD_NOT_FOUND
     */
    private EatRecord findRecordOrFail(Long recordId, Long userId) {
        LambdaQueryWrapper<EatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EatRecord::getId, recordId)
                .eq(EatRecord::getUserId, userId);
        EatRecord record = eatRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        return record;
    }

    /**
     * 构建响应 DTO
     */
    private EatRecordResponse buildResponse(EatRecord record, String foodName, String category) {
        return EatRecordResponse.builder()
                .id(record.getId())
                .foodId(record.getFoodId())
                .foodName(foodName)
                .mealType(record.getMealType())
                .status(record.getStatus())
                .rating(record.getRating())
                .note(record.getNote())
                .eatenAt(record.getEatenAt())
                .decidedAt(record.getDecidedAt())
                .category(category)
                .build();
    }
}

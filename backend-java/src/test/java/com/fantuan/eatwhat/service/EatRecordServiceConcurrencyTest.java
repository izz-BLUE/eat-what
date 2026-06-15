package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.EatRecordStatus;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.domain.entity.User;
import com.fantuan.eatwhat.dto.request.CompleteRecordRequest;
import com.fantuan.eatwhat.dto.request.DecideRecordRequest;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
import com.fantuan.eatwhat.dto.response.EatRecordResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.EatRecordMapper;
import com.fantuan.eatwhat.mapper.FoodMapper;
import com.fantuan.eatwhat.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 真实并发集成测试（H2 数据库，不 Mock Mapper）
 *
 * 两种测试模式：
 * 1. 顺序控制测试（latch 明确先后）— 验证每种合法顺序的期望行为
 * 2. 并发竞态测试（同时起跑）    — 验证用户行锁串行化效果
 *
 * 每条测试额外断言：
 * - 每用户最多 1 条 DECIDED
 * - DECIDED → eatenAt 必须为 null
 * - EATEN   → eatenAt 必须非 null
 * - 成功响应对应的记录确实存在于 DB 且处于正确状态
 */
@SpringBootTest
class EatRecordServiceConcurrencyTest {

    @Autowired private EatRecordService eatRecordService;
    @Autowired private UserMapper userMapper;
    @Autowired private FoodMapper foodMapper;
    @Autowired private EatRecordMapper eatRecordMapper;

    private static final Long UID = 9000L;
    private static final Long F1  = 9001L;  // 猪脚饭
    private static final Long F2  = 9002L;  // 黄焖鸡
    private static final Long F3  = 9003L;  // 麻辣烫

    // ==================== 生命周期 ====================

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UID);
        user.setOpenid("test_conc_" + System.nanoTime());
        user.setNickname("并发测试");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        createFood(F1, "并发测试猪脚饭");
        createFood(F2, "并发测试黄焖鸡");
        createFood(F3, "并发测试麻辣烫");
    }

    private void createFood(Long id, String name) {
        Food f = new Food();
        f.setId(id); f.setName(name); f.setCategory("快餐");
        f.setEnabled(true);
        f.setCreatedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        foodMapper.insert(f);
    }

    @AfterEach
    void tearDown() {
        LambdaQueryWrapper<EatRecord> w = new LambdaQueryWrapper<>();
        w.eq(EatRecord::getUserId, UID);
        eatRecordMapper.delete(w);
        foodMapper.deleteById(F1);
        foodMapper.deleteById(F2);
        foodMapper.deleteById(F3);
        userMapper.deleteById(UID);
    }

    // ==================== 断言工具 ====================

    /** 获取全量记录 */
    private List<EatRecord> allRecords() {
        LambdaQueryWrapper<EatRecord> w = new LambdaQueryWrapper<>();
        w.eq(EatRecord::getUserId, UID);
        return eatRecordMapper.selectList(w);
    }

    /** 按 id 获取单条记录（无则 null） */
    private EatRecord getRecord(Long id) {
        return eatRecordMapper.selectById(id);
    }

    /** DECIDED 计数 */
    private long countDecided() {
        LambdaQueryWrapper<EatRecord> w = new LambdaQueryWrapper<>();
        w.eq(EatRecord::getUserId, UID).eq(EatRecord::getStatus, EatRecordStatus.DECIDED);
        return eatRecordMapper.selectCount(w);
    }

    /**
     * 全量一致性断言：
     * - 最多 1 条 DECIDED
     * - DECIDED → eatenAt == null
     * - EATEN   → eatenAt != null
     */
    private void assertConsistency() {
        List<EatRecord> records = allRecords();
        long decidedCount = 0;
        for (EatRecord r : records) {
            if (EatRecordStatus.DECIDED.equals(r.getStatus())) {
                decidedCount++;
                assertNull(r.getEatenAt(),
                        "DECIDED 记录的 eatenAt 必须为 null, id=" + r.getId());
            } else if (EatRecordStatus.EATEN.equals(r.getStatus())) {
                assertNotNull(r.getEatenAt(),
                        "EATEN 记录的 eatenAt 必须非 null, id=" + r.getId());
            }
        }
        assertTrue(decidedCount <= 1,
                "最多 1 条 DECIDED，实际: " + decidedCount);
    }

    /**
     * 验证响应对应的记录确实存在于 DB，且状态一致
     */
    private void assertResponseRecordExists(EatRecordResponse resp) {
        assertNotNull(resp, "响应不能为 null");
        assertNotNull(resp.getId(), "响应 id 不能为 null");
        EatRecord db = getRecord(resp.getId());
        assertNotNull(db, "响应 recordId=" + resp.getId() + " 应存在于 DB");
        assertEquals(resp.getStatus(), db.getStatus(),
                "响应 status 与 DB 不一致, recordId=" + resp.getId());
        assertEquals(resp.getFoodId(), db.getFoodId(),
                "响应 foodId 与 DB 不一致, recordId=" + resp.getId());
    }

    // ==================== decide vs decide（并发竞态） ====================

    @Test
    void concurrentDecideSameFood_shouldBeIdempotent() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<EatRecordResponse> results = Collections.synchronizedList(new ArrayList<>());
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 2; i++) {
            ex.submit(() -> {
                try { go.await();
                    DecideRecordRequest r = new DecideRecordRequest();
                    r.setFoodId(F1); r.setMealType("午餐");
                    results.add(eatRecordService.createDecision(UID, r));
                } catch (Exception e) { errors.add(e); }
                finally { done.countDown(); }
            });
        }
        go.countDown(); done.await(30, TimeUnit.SECONDS); ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertEquals(2, results.size());
        assertEquals(results.get(0).getId(), results.get(1).getId(), "同菜品应返回同一 recordId");
        assertEquals(EatRecordStatus.DECIDED, results.get(0).getStatus());
        assertEquals(1, countDecided());
        assertConsistency();
        results.forEach(this::assertResponseRecordExists);
    }

    @Test
    void concurrentDecideDifferentFood_shouldKeepOnlyOne() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<EatRecordResponse> results = Collections.synchronizedList(new ArrayList<>());
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
        Long[] foods = {F1, F2};

        for (int i = 0; i < 2; i++) {
            final int idx = i;
            ex.submit(() -> {
                try { go.await();
                    DecideRecordRequest r = new DecideRecordRequest();
                    r.setFoodId(foods[idx]); r.setMealType("午餐");
                    results.add(eatRecordService.createDecision(UID, r));
                } catch (Exception e) { errors.add(e); }
                finally { done.countDown(); }
            });
        }
        go.countDown(); done.await(30, TimeUnit.SECONDS); ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertEquals(2, results.size());
        assertEquals(1, countDecided(), "恰好 1 条 DECIDED");

        // 只有一个调用的记录最终保留在 DB（另一个被替换删除）
        LambdaQueryWrapper<EatRecord> w = new LambdaQueryWrapper<>();
        w.eq(EatRecord::getUserId, UID).eq(EatRecord::getStatus, EatRecordStatus.DECIDED);
        EatRecord dbDecided = eatRecordMapper.selectOne(w);
        assertNotNull(dbDecided);

        // 保留的响应对应的记录必须存在于 DB
        long survivingCount = results.stream()
                .filter(r -> r.getId().equals(dbDecided.getId()))
                .peek(this::assertResponseRecordExists)
                .count();
        assertEquals(1, survivingCount, "恰好一个响应对应最终保留的记录");

        // 被替换的响应对应的记录应已不存在
        results.stream()
                .filter(r -> !r.getId().equals(dbDecided.getId()))
                .forEach(r -> assertNull(getRecord(r.getId()),
                        "被替换的 recordId=" + r.getId() + " 应已删除"));

        assertConsistency();
    }

    @Test
    void sequentialDecideDifferentFood_shouldReplacePrevious() {
        DecideRecordRequest r1 = new DecideRecordRequest();
        r1.setFoodId(F1); r1.setMealType("午餐");
        EatRecordResponse resp1 = eatRecordService.createDecision(UID, r1);
        assertEquals(F1, resp1.getFoodId());
        assertEquals(1, countDecided());
        assertResponseRecordExists(resp1);

        DecideRecordRequest r2 = new DecideRecordRequest();
        r2.setFoodId(F2); r2.setMealType("午餐");
        EatRecordResponse resp2 = eatRecordService.createDecision(UID, r2);
        assertEquals(F2, resp2.getFoodId());
        assertEquals(1, countDecided());

        // 旧 DECIDED 已删除
        assertNull(getRecord(resp1.getId()), "旧 DECIDED 应已被删除");
        assertResponseRecordExists(resp2);
        assertConsistency();
    }

    // ==================== decide vs eat（顺序控制） ====================

    /**
     * 顺序：先 decide，后 eat
     * decide 创建 DECIDED → eat 清理 DECIDED 并创建 EATEN
     * 预期：0 条 DECIDED，≥1 条 EATEN
     */
    @Test
    void decideThenEat_shouldClearDecided() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch decideGo    = new CountDownLatch(1);
        CountDownLatch decideDone  = new CountDownLatch(1);
        CountDownLatch eatDone     = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> decideResp = new AtomicReference<>();
        AtomicReference<EatRecordResponse> eatResp    = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: decide，先执行
        ex.submit(() -> {
            try { decideGo.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F1); r.setMealType("午餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        // 线程 B: eat，等待 decide 完成
        ex.submit(() -> {
            try { decideDone.await();  // 等 decide 完成
                EatRecordRequest r = new EatRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐"); r.setRating(4);
                eatResp.set(eatRecordService.createRecord(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { eatDone.countDown(); }
        });

        decideGo.countDown();
        eatDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertNotNull(decideResp.get());
        assertNotNull(eatResp.get());

        assertEquals(0, countDecided(), "eat 后不应有 DECIDED");
        assertNull(getRecord(decideResp.get().getId()), "原 DECIDED 应已被 eat 删除");
        assertResponseRecordExists(eatResp.get());
        assertEquals(EatRecordStatus.EATEN, eatResp.get().getStatus());
        assertConsistency();
    }

    /**
     * 顺序：先 eat，后 decide
     * eat 创建 EATEN → decide 创建 DECIDED
     * 预期：1 条 DECIDED，≥1 条 EATEN，两者互不干扰
     */
    @Test
    void eatThenDecide_shouldHaveNewDecided() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch eatGo      = new CountDownLatch(1);
        CountDownLatch eatDone    = new CountDownLatch(1);
        CountDownLatch decideDone = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> eatResp    = new AtomicReference<>();
        AtomicReference<EatRecordResponse> decideResp = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: eat，先执行
        ex.submit(() -> {
            try { eatGo.await();
                EatRecordRequest r = new EatRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐"); r.setRating(4);
                eatResp.set(eatRecordService.createRecord(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { eatDone.countDown(); }
        });

        // 线程 B: decide，等待 eat 完成
        ex.submit(() -> {
            try { eatDone.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F1); r.setMealType("午餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        eatGo.countDown();
        decideDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertNotNull(eatResp.get());
        assertNotNull(decideResp.get());

        assertEquals(1, countDecided(), "eat 后再 decide 应有 1 条 DECIDED");
        assertEquals(F1, decideResp.get().getFoodId());
        assertEquals(EatRecordStatus.DECIDED, decideResp.get().getStatus());
        assertResponseRecordExists(eatResp.get());
        assertResponseRecordExists(decideResp.get());
        assertConsistency();
    }

    // ==================== decide vs complete（顺序控制） ====================

    /**
     * 顺序：先 complete（完成初始 DECIDED），后 decide（创建新 DECIDED）
     * 预期：initial → EATEN，新 DECIDED 存在（1 条）
     */
    @Test
    void completeThenDecide_shouldReplaceCompleted() throws Exception {
        // 先创建初始 DECIDED
        DecideRecordRequest init = new DecideRecordRequest();
        init.setFoodId(F1); init.setMealType("午餐");
        EatRecordResponse initResp = eatRecordService.createDecision(UID, init);
        Long initId = initResp.getId();
        assertEquals(1, countDecided());

        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch completeGo   = new CountDownLatch(1);
        CountDownLatch completeDone = new CountDownLatch(1);
        CountDownLatch decideDone   = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> completeResp = new AtomicReference<>();
        AtomicReference<EatRecordResponse> decideResp   = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: complete，先执行
        ex.submit(() -> {
            try { completeGo.await();
                CompleteRecordRequest r = new CompleteRecordRequest();
                r.setRating(5);
                completeResp.set(eatRecordService.completeRecord(UID, initId, r));
            } catch (Exception e) { errors.add(e); }
            finally { completeDone.countDown(); }
        });

        // 线程 B: decide，等 complete 完成
        ex.submit(() -> {
            try { completeDone.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        completeGo.countDown();
        decideDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertNotNull(completeResp.get());
        assertNotNull(decideResp.get());

        // 初始记录已变为 EATEN
        assertEquals(EatRecordStatus.EATEN, completeResp.get().getStatus());
        assertNotNull(completeResp.get().getEatenAt());
        assertEquals(EatRecordStatus.EATEN, getRecord(initId).getStatus());

        // 新 DECIDED 存在
        assertEquals(1, countDecided(), "complete 后再 decide 应有 1 条 DECIDED");
        assertEquals(F2, decideResp.get().getFoodId());
        assertEquals(EatRecordStatus.DECIDED, decideResp.get().getStatus());

        assertResponseRecordExists(completeResp.get());
        assertResponseRecordExists(decideResp.get());
        assertConsistency();
    }

    /**
     * 顺序：先 decide（替换初始 DECIDED），后 complete 旧记录
     * 预期：旧记录已删除 → complete 抛 BusinessException，decide 成功创建新 DECIDED
     */
    @Test
    void decideThenComplete_shouldFailComplete() throws Exception {
        // 先创建初始 DECIDED
        DecideRecordRequest init = new DecideRecordRequest();
        init.setFoodId(F1); init.setMealType("午餐");
        EatRecordResponse initResp = eatRecordService.createDecision(UID, init);
        Long initId = initResp.getId();
        assertEquals(1, countDecided());

        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch decideGo     = new CountDownLatch(1);
        CountDownLatch decideDone   = new CountDownLatch(1);
        CountDownLatch completeDone = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> decideResp   = new AtomicReference<>();
        AtomicReference<Throwable>         completeErr  = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: decide(F2)，先执行（替换 F1 的 DECIDED）
        ex.submit(() -> {
            try { decideGo.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        // 线程 B: complete(initId)，等 decide 完成后执行
        ex.submit(() -> {
            try { decideDone.await();
                CompleteRecordRequest r = new CompleteRecordRequest();
                r.setRating(5);
                eatRecordService.completeRecord(UID, initId, r);
            } catch (BusinessException e) {
                completeErr.set(e);  // 预期
            } catch (Exception e) {
                errors.add(e);
            } finally { completeDone.countDown(); }
        });

        decideGo.countDown();
        completeDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应有非预期异常: " + errors);
        assertNotNull(decideResp.get());

        // complete 预期失败
        assertNotNull(completeErr.get(), "complete 应抛出 BusinessException");
        assertTrue(completeErr.get() instanceof BusinessException);

        // 旧 DECIDED 已被 decide 删除
        assertNull(getRecord(initId), "旧 DECIDED 应已被 decide 删除");

        // 新 DECIDED 存在
        assertEquals(1, countDecided(), "替换 decide 后应有 1 条 DECIDED");
        assertEquals(F2, decideResp.get().getFoodId());
        assertResponseRecordExists(decideResp.get());
        assertConsistency();
    }

    // ==================== decide vs cancel（顺序控制） ====================

    /**
     * 顺序：先 cancel 初始 DECIDED，后 decide
     * 预期：cancel 成功（记录删除），decide 创建新 DECIDED（1 条）
     */
    @Test
    void cancelThenDecide_shouldCreateNewDecided() throws Exception {
        DecideRecordRequest init = new DecideRecordRequest();
        init.setFoodId(F1); init.setMealType("午餐");
        EatRecordResponse initResp = eatRecordService.createDecision(UID, init);
        Long initId = initResp.getId();
        assertEquals(1, countDecided());

        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch cancelGo   = new CountDownLatch(1);
        CountDownLatch cancelDone = new CountDownLatch(1);
        CountDownLatch decideDone = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> decideResp = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: cancel，先执行
        ex.submit(() -> {
            try { cancelGo.await();
                eatRecordService.cancelDecision(UID, initId);
            } catch (Exception e) { errors.add(e); }
            finally { cancelDone.countDown(); }
        });

        // 线程 B: decide，等 cancel 完成
        ex.submit(() -> {
            try { cancelDone.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        cancelGo.countDown();
        decideDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应抛异常: " + errors);
        assertNotNull(decideResp.get());

        // 原记录已删除
        assertNull(getRecord(initId), "原 DECIDED 应已被 cancel 删除");

        // 新 DECIDED 存在
        assertEquals(1, countDecided(), "cancel 后再 decide 应有 1 条 DECIDED");
        assertEquals(F2, decideResp.get().getFoodId());
        assertResponseRecordExists(decideResp.get());
        assertConsistency();
    }

    /**
     * 顺序：先 decide（替换初始 DECIDED），后 cancel 旧记录
     * 预期：旧记录已删除 → cancel 抛 BusinessException，decide 成功创建新 DECIDED
     */
    @Test
    void decideThenCancel_shouldFailCancel() throws Exception {
        DecideRecordRequest init = new DecideRecordRequest();
        init.setFoodId(F1); init.setMealType("午餐");
        EatRecordResponse initResp = eatRecordService.createDecision(UID, init);
        Long initId = initResp.getId();
        assertEquals(1, countDecided());

        ExecutorService ex = Executors.newFixedThreadPool(2);
        CountDownLatch decideGo    = new CountDownLatch(1);
        CountDownLatch decideDone  = new CountDownLatch(1);
        CountDownLatch cancelDone  = new CountDownLatch(1);
        AtomicReference<EatRecordResponse> decideResp  = new AtomicReference<>();
        AtomicReference<Throwable>         cancelErr   = new AtomicReference<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // 线程 A: decide(F2)，先执行（替换 F1 的 DECIDED）
        ex.submit(() -> {
            try { decideGo.await();
                DecideRecordRequest r = new DecideRecordRequest();
                r.setFoodId(F2); r.setMealType("晚餐");
                decideResp.set(eatRecordService.createDecision(UID, r));
            } catch (Exception e) { errors.add(e); }
            finally { decideDone.countDown(); }
        });

        // 线程 B: cancel(initId)，等 decide 完成后执行
        ex.submit(() -> {
            try { decideDone.await();
                eatRecordService.cancelDecision(UID, initId);
            } catch (BusinessException e) {
                cancelErr.set(e);  // 预期
            } catch (Exception e) {
                errors.add(e);
            } finally { cancelDone.countDown(); }
        });

        decideGo.countDown();
        cancelDone.await(30, TimeUnit.SECONDS);
        ex.shutdown();

        assertTrue(errors.isEmpty(), "不应有非预期异常: " + errors);
        assertNotNull(decideResp.get());

        // cancel 预期失败
        assertNotNull(cancelErr.get(), "cancel 应抛出 BusinessException");
        assertTrue(cancelErr.get() instanceof BusinessException);

        // 旧 DECIDED 已被 decide 删除
        assertNull(getRecord(initId), "旧 DECIDED 应已被 decide 删除");

        // 新 DECIDED 存在
        assertEquals(1, countDecided(), "替换 decide 后应有 1 条 DECIDED");
        assertEquals(F2, decideResp.get().getFoodId());
        assertResponseRecordExists(decideResp.get());
        assertConsistency();
    }

    // ==================== 幂等：同 foodId 不同 mealType ====================

    @Test
    void sameFoodDifferentMealType_shouldUpdateMealTypeNotReplace() {
        DecideRecordRequest r1 = new DecideRecordRequest();
        r1.setFoodId(F1); r1.setMealType("午餐");
        EatRecordResponse resp1 = eatRecordService.createDecision(UID, r1);
        assertEquals(F1, resp1.getFoodId());
        assertEquals("午餐", resp1.getMealType());
        Long id = resp1.getId();

        DecideRecordRequest r2 = new DecideRecordRequest();
        r2.setFoodId(F1); r2.setMealType("晚餐");
        EatRecordResponse resp2 = eatRecordService.createDecision(UID, r2);

        assertEquals(id, resp2.getId(), "应返回同一 recordId");
        assertEquals("晚餐", resp2.getMealType(), "mealType 应更新");
        assertEquals(1, countDecided(), "不应创建第二条");
        assertResponseRecordExists(resp2);
        assertConsistency();
    }

    @Test
    void sameFoodSameMealType_shouldBePureIdempotent() {
        DecideRecordRequest r1 = new DecideRecordRequest();
        r1.setFoodId(F1); r1.setMealType("午餐");
        EatRecordResponse resp1 = eatRecordService.createDecision(UID, r1);
        Long id = resp1.getId();
        LocalDateTime decidedAt1 = resp1.getDecidedAt();

        DecideRecordRequest r2 = new DecideRecordRequest();
        r2.setFoodId(F1); r2.setMealType("午餐");
        EatRecordResponse resp2 = eatRecordService.createDecision(UID, r2);

        assertEquals(id, resp2.getId(), "应返回同一 recordId");
        assertEquals("午餐", resp2.getMealType());
        assertEquals(1, countDecided());
        // decidedAt 不应改变（幂等，不调用 updateById）
        // 注意：H2 存储时可能截断纳秒精度，使用 truncatedTo 对齐
        assertEquals(
                decidedAt1.truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
                getRecord(id).getDecidedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
                "decidedAt 应不变（幂等）");
        assertResponseRecordExists(resp2);
        assertConsistency();
    }
}

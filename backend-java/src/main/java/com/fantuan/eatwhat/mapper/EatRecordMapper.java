package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 吃过记录 Mapper
 */
@Mapper
public interface EatRecordMapper extends BaseMapper<EatRecord> {

    /**
     * 查询用户最近吃过的食物ID和最近吃的时间（仅 EATEN 状态）
     *
     * @param userId   用户ID
     * @param since    起始时间
     * @return 食物ID和最近吃的时间列表
     */
    @Select("SELECT food_id AS foodId, MAX(eaten_at) AS lastEatenAt " +
            "FROM eat_records " +
            "WHERE user_id = #{userId} AND eaten_at >= #{since} AND status = 'EATEN' " +
            "GROUP BY food_id")
    List<Map<String, Object>> selectRecentEatenFoods(@Param("userId") Long userId,
                                                      @Param("since") LocalDateTime since);

    /**
     * 锁定并查询用户当前的 DECIDED 记录（FOR UPDATE）
     *
     * @param userId 用户ID
     * @return DECIDED 记录，无则 null
     */
    @Select("SELECT * FROM eat_records " +
            "WHERE user_id = #{userId} AND status = 'DECIDED' " +
            "FOR UPDATE")
    EatRecord selectDecidedForUpdate(@Param("userId") Long userId);

    /**
     * 查询用户有评分的已吃记录（仅 EATEN 状态，rating 非空）
     *
     * @param userId 用户ID
     * @return 有评分的已吃记录列表
     */
    @Select("SELECT * FROM eat_records " +
            "WHERE user_id = #{userId} AND status = 'EATEN' AND rating IS NOT NULL")
    List<EatRecord> selectRatedEatenRecords(@Param("userId") Long userId);

    /**
     * 查询用户最近吃过的自定义食物ID和最近吃的时间（仅 EATEN 状态，food_source='CUSTOM'）
     *
     * @param userId 用户ID
     * @param since  起始时间
     * @return 自定义食物ID和最近吃的时间列表
     */
    @Select("SELECT custom_food_id AS customFoodId, MAX(eaten_at) AS lastEatenAt " +
            "FROM eat_records " +
            "WHERE user_id = #{userId} AND eaten_at >= #{since} AND status = 'EATEN' " +
            "AND food_source = 'CUSTOM' " +
            "GROUP BY custom_food_id")
    List<Map<String, Object>> selectRecentEatenCustomFoods(@Param("userId") Long userId,
                                                           @Param("since") LocalDateTime since);
}

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
     * 查询用户最近吃过的食物ID和最近吃的时间
     *
     * @param userId   用户ID
     * @param since    起始时间
     * @return 食物ID和最近吃的时间列表
     */
    @Select("SELECT food_id AS foodId, MAX(eaten_at) AS lastEatenAt " +
            "FROM eat_records " +
            "WHERE user_id = #{userId} AND eaten_at >= #{since} " +
            "GROUP BY food_id")
    List<Map<String, Object>> selectRecentEatenFoods(@Param("userId") Long userId,
                                                      @Param("since") LocalDateTime since);
}

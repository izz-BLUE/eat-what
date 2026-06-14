package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.UserDislike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户不想吃 Mapper
 */
@Mapper
public interface UserDislikeMapper extends BaseMapper<UserDislike> {

    /**
     * 查询用户有效的不想吃分类
     */
    @Select("SELECT category FROM user_dislikes WHERE user_id = #{userId} AND expires_at > #{now}")
    List<String> selectActiveCategories(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

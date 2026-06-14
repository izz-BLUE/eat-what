package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.UserBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户黑名单 Mapper
 */
@Mapper
public interface UserBlacklistMapper extends BaseMapper<UserBlacklist> {

    /**
     * 查询用户所有黑名单食物ID
     */
    @Select("SELECT food_id FROM user_blacklist WHERE user_id = #{userId}")
    List<Long> selectFoodIdsByUserId(@Param("userId") Long userId);
}

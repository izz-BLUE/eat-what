package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 锁定用户行（FOR UPDATE），用于并发控制
     * 用户行一定存在，确保并发场景下总能获取到锁
     *
     * @param userId 用户ID
     * @return 用户ID（用于确认锁已获取）
     */
    @Select("SELECT id FROM users WHERE id = #{userId} FOR UPDATE")
    Long selectUserIdForUpdate(@Param("userId") Long userId);
}

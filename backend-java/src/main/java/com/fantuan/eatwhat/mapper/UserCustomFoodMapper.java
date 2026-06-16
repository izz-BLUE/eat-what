package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.UserCustomFood;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户自定义菜品 Mapper
 */
@Mapper
public interface UserCustomFoodMapper extends BaseMapper<UserCustomFood> {
}

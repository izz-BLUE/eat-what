package com.fantuan.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fantuan.eatwhat.domain.entity.Food;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品 Mapper
 */
@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}

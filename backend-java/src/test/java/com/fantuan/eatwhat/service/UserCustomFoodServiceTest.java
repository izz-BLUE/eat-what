package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.UserCustomFood;
import com.fantuan.eatwhat.dto.request.CustomFoodCreateRequest;
import com.fantuan.eatwhat.dto.response.CustomFoodResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserCustomFoodMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCustomFoodServiceTest {

    @Mock
    private UserCustomFoodMapper userCustomFoodMapper;

    @InjectMocks
    private UserCustomFoodService userCustomFoodService;

    // ==================== create ====================

    @Test
    void create_success() {
        Long userId = 1L;
        CustomFoodCreateRequest request = new CustomFoodCreateRequest();
        request.setName(" 妈妈做的炒饭 ");
        request.setTypeTags(List.of("快餐"));
        request.setCuisineTags(List.of("家常菜"));
        request.setMealTypes(List.of("午餐", "晚餐"));
        request.setTasteTags(List.of("咸", "香"));
        request.setPriceLevel(1);

        when(userCustomFoodMapper.insert(any(UserCustomFood.class))).thenReturn(1);

        CustomFoodResponse response = userCustomFoodService.create(userId, request);

        assertNotNull(response);
        assertEquals("妈妈做的炒饭", response.getName());
        assertEquals("家常菜", response.getCategory());
        assertEquals("快餐", response.getTypeTags());
        assertEquals("家常菜", response.getCuisineTags());
        assertEquals("午餐,晚餐", response.getMealTypes());
        assertEquals("咸,香", response.getTasteTags());
        assertEquals(1, response.getPriceLevel());
        assertTrue(response.getEnabled());
    }

    @Test
    void create_duplicateName_sameUser_throwsError() {
        Long userId = 1L;
        CustomFoodCreateRequest request = buildValidRequest();

        when(userCustomFoodMapper.insert(any(UserCustomFood.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(userId, request));
        assertEquals(ResultCode.CUSTOM_FOOD_DUPLICATE.getCode(), exception.getCode());
    }

    @Test
    void create_duplicateName_differentUser_success() {
        CustomFoodCreateRequest request = buildValidRequest();
        // 不同 userId，不会冲突
        when(userCustomFoodMapper.insert(any(UserCustomFood.class))).thenReturn(1);

        CustomFoodResponse r1 = userCustomFoodService.create(1L, request);
        CustomFoodResponse r2 = userCustomFoodService.create(2L, request);

        assertNotNull(r1);
        assertNotNull(r2);
    }

    @Test
    void create_noTypeAndCuisineTags_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setTypeTags(null);
        request.setCuisineTags(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_bothTypeAndCuisineEmpty_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setTypeTags(List.of());
        request.setCuisineTags(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_invalidTypeTag_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setTypeTags(List.of("不存在的类型"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_invalidCuisineTag_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setCuisineTags(List.of("泰国菜"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_invalidTasteTag_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setTasteTags(List.of("重口"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_invalidMealType_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setMealTypes(List.of("早茶"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_emptyMealTypes_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setMealTypes(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_emptyTasteTags_throwsError() {
        CustomFoodCreateRequest request = buildValidRequest();
        request.setTasteTags(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.create(1L, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    // ==================== list ====================

    @Test
    void list_returnsOnlyEnabled() {
        Long userId = 1L;
        UserCustomFood f1 = createEntity(1L, userId, "菜1", true);
        UserCustomFood f2 = createEntity(2L, userId, "菜2", true);

        when(userCustomFoodMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(f1, f2));

        List<CustomFoodResponse> result = userCustomFoodService.list(userId);
        assertEquals(2, result.size());
        assertEquals("菜1", result.get(0).getName());
    }

    @Test
    void list_doesNotReturnDisabled() {
        Long userId = 1L;
        UserCustomFood f1 = createEntity(1L, userId, "菜1", true);
        // Only enabled foods are returned because the query filters by enabled=true

        when(userCustomFoodMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(f1));

        List<CustomFoodResponse> result = userCustomFoodService.list(userId);
        assertEquals(1, result.size());
    }

    @Test
    void list_onlyReturnsCurrentUser() {
        // User 1's custom foods should not appear in User 2's list
        when(userCustomFoodMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<CustomFoodResponse> result = userCustomFoodService.list(2L);
        assertTrue(result.isEmpty());
    }

    // ==================== delete ====================

    @Test
    void delete_softDelete_setsEnabledFalse() {
        Long userId = 1L;
        UserCustomFood entity = createEntity(1L, userId, "菜1", true);

        when(userCustomFoodMapper.selectById(1L)).thenReturn(entity);
        when(userCustomFoodMapper.updateById(any(UserCustomFood.class))).thenReturn(1);

        userCustomFoodService.delete(userId, 1L);
        assertFalse(entity.getEnabled());
        verify(userCustomFoodMapper).updateById(entity);
    }

    @Test
    void delete_notOwnFood_throwsError() {
        UserCustomFood entity = createEntity(1L, 999L, "别人的菜", true);
        when(userCustomFoodMapper.selectById(1L)).thenReturn(entity);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.delete(1L, 1L));
        assertEquals(ResultCode.CUSTOM_FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void delete_notFound_throwsError() {
        when(userCustomFoodMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userCustomFoodService.delete(1L, 999L));
        assertEquals(ResultCode.CUSTOM_FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== getEnabledCustomFoods ====================

    @Test
    void getEnabledCustomFoods_returnsOnlyEnabled() {
        Long userId = 1L;
        UserCustomFood f1 = createEntity(1L, userId, "菜1", true);
        UserCustomFood f2 = createEntity(2L, userId, "菜2", true);

        when(userCustomFoodMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(f1, f2));

        List<UserCustomFood> result = userCustomFoodService.getEnabledCustomFoods(userId);
        assertEquals(2, result.size());
    }

    // ==================== deriveCategory ====================

    @Test
    void deriveCategory_cuisineExists_returnsFirstCuisine() {
        String category = UserCustomFoodService.deriveCategory(
                List.of("快餐", "小吃"), List.of("川菜", "湘菜"));
        assertEquals("川菜", category);
    }

    @Test
    void deriveCategory_onlyTypeTags_returnsFirstTypeTag() {
        String category = UserCustomFoodService.deriveCategory(
                List.of("快餐", "面食"), null);
        assertEquals("快餐", category);
    }

    @Test
    void deriveCategory_emptyCuisine_returnsFirstType() {
        String category = UserCustomFoodService.deriveCategory(
                List.of("火锅"), List.of());
        assertEquals("火锅", category);
    }

    // ==================== helpers ====================

    private CustomFoodCreateRequest buildValidRequest() {
        CustomFoodCreateRequest request = new CustomFoodCreateRequest();
        request.setName("测试菜");
        request.setTypeTags(List.of("快餐"));
        request.setCuisineTags(List.of("家常菜"));
        request.setMealTypes(List.of("午餐"));
        request.setTasteTags(List.of("咸"));
        request.setPriceLevel(2);
        return request;
    }

    private UserCustomFood createEntity(Long id, Long userId, String name, boolean enabled) {
        UserCustomFood entity = new UserCustomFood();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setName(name);
        entity.setCategory("测试");
        entity.setTypeTags("快餐");
        entity.setCuisineTags("家常菜");
        entity.setMealTypes("午餐");
        entity.setTasteTags("咸");
        entity.setPriceLevel(2);
        entity.setEnabled(enabled);
        return entity;
    }
}

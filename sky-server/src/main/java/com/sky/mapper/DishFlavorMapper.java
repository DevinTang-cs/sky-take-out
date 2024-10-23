package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 删除口味表中关联的数据
     * @param dishId
     */
    @Delete("delete from sky_take_out.dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 批量删除口味表中关联的数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}

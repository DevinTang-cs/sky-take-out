package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.nio.file.LinkOption;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ID查询套餐ID
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


    void insertBatch(List<SetmealDish> setmealDishList);

    /**
     * 根据套餐id删除菜品
     * @param ids
     */
    void deleteBySetmealIds(List<Long> ids);

    /**
     * 根据套餐id删除菜品
     * @param id
     */
    @Delete("delete from sky_take_out.setmeal_dish where setmeal_id=#{id}")
    void deleteBySetmealId(Long id);
}

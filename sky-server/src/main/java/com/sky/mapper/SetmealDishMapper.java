package com.sky.mapper;

import com.sky.entity.SetmealDish;
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
}

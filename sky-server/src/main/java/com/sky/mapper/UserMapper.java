package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from sky_take_out.user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据主键查询用户
     * @param userId
     * @return
     */
    @Select("select * from sky_take_out.user where id=#{id}")
    User getById(Long userId);

    /**
     * 统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}

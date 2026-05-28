package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.UserProps;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户道具/背包 Mapper
 */
@Mapper
public interface UserPropsMapper extends BaseMapper<UserProps> {

    /**
     * 查询用户背包（按类型）
     */
    @Select("SELECT * FROM user_props WHERE volunteer_id = #{volunteerId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<UserProps> getUserBackpack(@Param("volunteerId") Long volunteerId);

    /**
     * 查询用户是否拥有某道具
     */
    @Select("SELECT * FROM user_props WHERE volunteer_id = #{volunteerId} AND prop_id = #{propId} AND prop_type = #{propType} AND is_deleted = 0 LIMIT 1")
    UserProps findByVolunteerAndProp(@Param("volunteerId") Long volunteerId, @Param("propId") Long propId,
            @Param("propType") String propType);

    /**
     * 增加道具数量
     */
    @Update("UPDATE user_props SET quantity = quantity + #{quantity}, update_time = NOW() WHERE id = #{id}")
    int increaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 减少道具数量
     */
    @Update("UPDATE user_props SET quantity = quantity - #{quantity}, update_time = NOW() WHERE id = #{id} AND quantity >= #{quantity}")
    int decreaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 按道具名称查询用户是否拥有
     */
    @Select("SELECT * FROM user_props WHERE volunteer_id = #{volunteerId} AND prop_name = #{propName} AND quantity > 0 AND is_deleted = 0 LIMIT 1")
    UserProps findByVolunteerAndPropName(@Param("volunteerId") Long volunteerId, @Param("propName") String propName);

    /**
     * 按道具类型查询用户是否拥有
     */
    @Select("SELECT * FROM user_props WHERE volunteer_id = #{volunteerId} AND prop_type = #{propType} AND quantity > 0 AND is_deleted = 0 LIMIT 1")
    UserProps findByVolunteerAndPropType(@Param("volunteerId") Long volunteerId, @Param("propType") String propType);
}

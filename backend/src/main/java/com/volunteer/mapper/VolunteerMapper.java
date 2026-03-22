package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 志愿者Mapper接口
 */
@Mapper
public interface VolunteerMapper extends BaseMapper<Volunteer> {

    /**
     * 原子扣减积分（包含乐观锁机制，防止扣成负数）
     * 
     * @param id 志愿者ID
     * @param pointsCost 扣减积分数
     * @return 影响行数 (如果余额不足则返回 0)
     */
    @Update("UPDATE volunteer SET available_points = available_points - #{pointsCost}, update_time = NOW() " +
            "WHERE id = #{id} AND available_points >= #{pointsCost}")
    int deductPoints(@Param("id") Long id, @Param("pointsCost") int pointsCost);

    /**
     * 原子增加积分
     * 
     * @param id 志愿者ID
     * @param points 增加积分数
     * @return 影响行数
     */
    @Update("UPDATE volunteer SET available_points = available_points + #{points}, total_points = total_points + #{points}, update_time = NOW() " +
            "WHERE id = #{id}")
    int addPoints(@Param("id") Long id, @Param("points") int points);
}

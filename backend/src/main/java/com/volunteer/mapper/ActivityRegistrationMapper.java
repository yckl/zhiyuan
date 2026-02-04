package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动报名Mapper接口
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    /**
     * 恢复软删除的报名记录
     */
    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true")
    @org.apache.ibatis.annotations.Update("UPDATE activity_registration SET is_deleted = 0, status = #{status}, update_time = NOW() WHERE activity_id = #{activityId} AND volunteer_id = #{volunteerId}")
    int restoreRegistration(@org.apache.ibatis.annotations.Param("activityId") Long activityId,
            @org.apache.ibatis.annotations.Param("volunteerId") Long volunteerId,
            @org.apache.ibatis.annotations.Param("status") Integer status);

    /**
     * 查找包括软删除在内的报名记录
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM activity_registration WHERE activity_id = #{activityId} AND volunteer_id = #{volunteerId} LIMIT 1")
    ActivityRegistration findAnyRegistration(@org.apache.ibatis.annotations.Param("activityId") Long activityId,
            @org.apache.ibatis.annotations.Param("volunteerId") Long volunteerId);
}

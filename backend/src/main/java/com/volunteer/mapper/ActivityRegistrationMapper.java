package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动报名Mapper接口
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

        /**
         * 恢复软删除的报名记录
         */
        @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true")
        @Update("UPDATE activity_registration SET is_deleted = 0, status = #{status}, update_time = NOW() WHERE activity_id = #{activityId} AND volunteer_id = #{volunteerId}")
        int restoreRegistration(@Param("activityId") Long activityId,
                        @Param("volunteerId") Long volunteerId,
                        @Param("status") Integer status);

        /**
         * 查找包括软删除在内的报名记录
         */
        @Select("SELECT * FROM activity_registration WHERE activity_id = #{activityId} AND volunteer_id = #{volunteerId} LIMIT 1")
        ActivityRegistration findAnyRegistration(@Param("activityId") Long activityId,
                        @Param("volunteerId") Long volunteerId);

        /**
         * 查找志愿者在指定时间段内有冲突的报名记录
         * 用于检测时间冲突：新活动的时间段与已报名活动是否重叠
         * 
         * @param volunteerId 志愿者ID
         * @param startTime   新活动开始时间
         * @param endTime     新活动结束时间
         * @return 存在时间冲突的报名记录列表
         */
        @Select("SELECT ar.* FROM activity_registration ar " +
                        "JOIN activity a ON ar.activity_id = a.id " +
                        "WHERE ar.volunteer_id = #{volunteerId} " +
                        "AND ar.status IN (0, 1, 2) " + // 已报名、已确认、已签到
                        "AND ar.is_deleted = 0 " +
                        "AND a.is_deleted = 0 " +
                        "AND a.start_time < #{endTime} " +
                        "AND a.end_time > #{startTime}")
        List<ActivityRegistration> findConflictingRegistrations(
                        @Param("volunteerId") Long volunteerId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}

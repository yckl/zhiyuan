package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

        /**
         * 使用乐观锁增加活动报名人数（集群安全）
         * 只有当 max_participants 为 0（无限制）或 current_participants < max_participants 时才成功
         * 
         * @param activityId 活动ID
         * @return 影响行数，0表示失败（人数已满或活动不存在）
         */
        @Update("UPDATE activity SET current_participants = current_participants + 1, " +
                        "update_time = NOW() " +
                        "WHERE id = #{activityId} " +
                        "AND is_deleted = 0 " +
                        "AND (max_participants = 0 OR current_participants < max_participants)")
        int incrementParticipants(@Param("activityId") Long activityId);

        /**
         * 减少活动报名人数
         * 
         * @param activityId 活动ID
         * @return 影响行数
         */
        @Update("UPDATE activity SET current_participants = current_participants - 1, " +
                        "update_time = NOW() " +
                        "WHERE id = #{activityId} " +
                        "AND is_deleted = 0 " +
                        "AND current_participants > 0")
        int decrementParticipants(@Param("activityId") Long activityId);

        /**
         * 批量将已到开始时间的已发布活动设为"进行中"
         * 状态 2(已发布) -> 3(进行中)
         * 
         * @param now 当前时间
         * @return 更新的活动数量
         */
        @Update("UPDATE activity SET status = 3, update_time = NOW() " +
                        "WHERE status = 2 " +
                        "AND is_deleted = 0 " +
                        "AND start_time <= #{now}")
        int batchUpdateToOngoing(@Param("now") LocalDateTime now);

        /**
         * 批量将已过结束时间的进行中活动设为"已结束"
         * 状态 3(进行中) -> 4(已结束)
         * 
         * @param now 当前时间
         * @return 更新的活动数量
         */
        @Update("UPDATE activity SET status = 4, update_time = NOW() " +
                        "WHERE status = 3 " +
                        "AND is_deleted = 0 " +
                        "AND end_time <= #{now}")
        int batchUpdateToEnded(@Param("now") LocalDateTime now);
}

package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CourseExamRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考试记录 Mapper
 */
@Mapper
public interface CourseExamRecordMapper extends BaseMapper<CourseExamRecord> {

        /**
         * 查询用户某课程的通过记录
         */
        @Select("SELECT * FROM course_exam_record WHERE volunteer_id = #{volunteerId} AND course_id = #{courseId} AND passed = 1 AND is_deleted = 0 LIMIT 1")
        CourseExamRecord findPassedRecord(@Param("volunteerId") Long volunteerId, @Param("courseId") Long courseId);

        /**
         * 查询用户某课程的最高分记录
         */
        @Select("SELECT * FROM course_exam_record WHERE volunteer_id = #{volunteerId} AND course_id = #{courseId} AND is_deleted = 0 ORDER BY score DESC LIMIT 1")
        CourseExamRecord findBestRecord(@Param("volunteerId") Long volunteerId, @Param("courseId") Long courseId);

        /**
         * 统计用户参加某课程考试次数
         */
        @Select("SELECT COUNT(*) FROM course_exam_record WHERE volunteer_id = #{volunteerId} AND course_id = #{courseId} AND is_deleted = 0")
        int countExamAttempts(@Param("volunteerId") Long volunteerId, @Param("courseId") Long courseId);

        /**
         * 获取用户已通过课程的总学时
         */
        @Select("SELECT SUM(c.credit_hours) FROM course c JOIN course_exam_record r ON c.id = r.course_id " +
                        "WHERE r.volunteer_id = #{volunteerId} AND r.passed = 1 AND r.is_deleted = 0 AND c.is_deleted = 0")
        BigDecimal sumPassedHours(@Param("volunteerId") Long volunteerId);

        /**
         * 获取用户最近一次参加的考试记录
         */
        /**
         * 获取用户最近一次参加的考试记录
         */
        @Select("SELECT * FROM course_exam_record WHERE volunteer_id = #{volunteerId} AND is_deleted = 0 " +
                        "ORDER BY submit_time DESC LIMIT 1")
        CourseExamRecord findLatestRecord(@Param("volunteerId") Long volunteerId);

        /**
         * 获取用户待考/未通过记录（含课程标题）
         */
        @Select("SELECT r.*, c.title as course_title FROM course_exam_record r " +
                        "JOIN course c ON r.course_id = c.id " +
                        "WHERE r.volunteer_id = #{volunteerId} AND r.passed = 0 AND r.is_deleted = 0 AND c.is_deleted = 0 "
                        +
                        "ORDER BY r.submit_time DESC LIMIT #{limit}")
        List<com.volunteer.vo.CourseExamRecordVO> findPendingExamsVO(@Param("volunteerId") Long volunteerId,
                        @Param("limit") int limit);

        /**
         * 获取用户所有考试记录（含课程标题）
         */
        @Select("<script>" +
                        "SELECT r.*, c.title as course_title FROM course_exam_record r " +
                        "JOIN course c ON r.course_id = c.id " +
                        "WHERE r.volunteer_id = #{volunteerId} AND r.is_deleted = 0 AND c.is_deleted = 0 " +
                        "<if test='courseId != null'> AND r.course_id = #{courseId} </if> " +
                        "ORDER BY r.submit_time DESC" +
                        "</script>")
        List<com.volunteer.vo.CourseExamRecordVO> findAllRecordsVO(@Param("volunteerId") Long volunteerId,
                        @Param("courseId") Long courseId);
}

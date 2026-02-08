package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CourseProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 课程学习进度Mapper接口
 */
@Mapper
public interface CourseProgressMapper extends BaseMapper<CourseProgress> {

    /**
     * 查询志愿者的课程学习进度
     */
    @Select("SELECT * FROM course_progress WHERE volunteer_id = #{volunteerId} AND course_id = #{courseId} AND is_deleted = 0 LIMIT 1")
    CourseProgress findByVolunteerAndCourse(@Param("volunteerId") Long volunteerId, @Param("courseId") Long courseId);
}

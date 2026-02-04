package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CourseQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 课程题目 Mapper
 */
@Mapper
public interface CourseQuestionMapper extends BaseMapper<CourseQuestion> {

    /**
     * 获取课程的所有启用题目
     */
    @Select("SELECT * FROM course_question WHERE course_id = #{courseId} AND status = 1 AND is_deleted = 0")
    List<CourseQuestion> getQuestionsByCourse(@Param("courseId") Long courseId);

    /**
     * 随机抽取指定数量的题目
     */
    @Select("SELECT * FROM course_question WHERE course_id = #{courseId} AND status = 1 AND is_deleted = 0 ORDER BY RAND() LIMIT #{count}")
    List<CourseQuestion> getRandomQuestions(@Param("courseId") Long courseId, @Param("count") int count);

    /**
     * 统计课程题目数量
     */
    @Select("SELECT COUNT(*) FROM course_question WHERE course_id = #{courseId} AND status = 1 AND is_deleted = 0")
    int countByCourse(@Param("courseId") Long courseId);
}

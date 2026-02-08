package com.volunteer.service;

import com.volunteer.entity.Course;
import com.volunteer.vo.ExamPaperVO;
import com.volunteer.vo.ExamResultVO;
import com.volunteer.vo.ExamSubmitDTO;

import java.util.List;

/**
 * 培训考试服务接口
 */
public interface CourseExamService {

    /**
     * 获取课程列表
     */
    List<Course> getCourseList(String category);

    /**
     * 获取课程详情
     */
    Course getCourseDetail(Long courseId);

    /**
     * 生成试卷（随机抽取题目，不含答案）
     * 
     * @param courseId      课程ID
     * @param questionCount 题目数量（默认10）
     * @return 试卷（题目列表）
     */
    ExamPaperVO generateExamPaper(Long courseId, int questionCount);

    /**
     * 提交试卷
     * 
     * @param submitDTO 提交的答案
     * @return 考试结果（得分、是否通过、奖励积分）
     */
    ExamResultVO submitExam(ExamSubmitDTO submitDTO);

    /**
     * 获取考试记录
     */
    List<com.volunteer.vo.CourseExamRecordVO> getExamRecords(Long courseId);

    /**
     * 检查是否已通过考试
     */
    boolean hasPassedExam(Long courseId);

    /**
     * 获取学习统计数据
     */
    com.volunteer.vo.TrainingStatsVO getTrainingStats();

    /**
     * 获取继续学习的课程
     */
    Course getResumeCourse();

    /**
     * 获取必修课程列表
     */
    List<Course> getMandatoryCourses();

    /**
     * 获取待考提醒列表
     */
    List<com.volunteer.vo.CourseExamRecordVO> getPendingExams();
}

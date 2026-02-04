package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Course;
import com.volunteer.service.CourseExamService;
import com.volunteer.vo.ExamPaperVO;
import com.volunteer.vo.ExamResultVO;
import com.volunteer.vo.ExamSubmitDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 培训考试控制器
 */
@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@Tag(name = "培训考试", description = "课程列表、生成试卷、提交考试")
public class CourseExamController {

    private final CourseExamService examService;

    // ==================== 课程相关 ====================

    /**
     * 获取课程列表
     */
    @GetMapping("/list")
    @Operation(summary = "课程列表", description = "获取培训课程列表")
    public Result<List<Course>> getCourseList(
            @Parameter(description = "分类") @RequestParam(required = false) String category) {
        try {
            List<Course> list = examService.getCourseList(category);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取课程列表失败:", e);
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "课程详情", description = "获取课程详细信息")
    public Result<Course> getCourseDetail(@PathVariable Long id) {
        try {
            Course course = examService.getCourseDetail(id);
            if (course == null) {
                return Result.error("课程不存在");
            }
            return Result.success(course);
        } catch (Exception e) {
            log.error("获取课程详情失败:", e);
            return Result.error("获取课程详情失败: " + e.getMessage());
        }
    }

    // ==================== 考试相关 ====================

    /**
     * 生成试卷
     */
    @GetMapping("/exam/paper")
    @Operation(summary = "生成试卷", description = "随机抽取题目生成试卷")
    public Result<ExamPaperVO> generateExamPaper(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "题目数量") @RequestParam(defaultValue = "10") int count) {
        try {
            ExamPaperVO paper = examService.generateExamPaper(courseId, count);
            return Result.success(paper);
        } catch (Exception e) {
            log.error("生成试卷失败:", e);
            return Result.error("生成试卷失败: " + e.getMessage());
        }
    }

    /**
     * 提交试卷
     */
    @PostMapping("/exam/submit")
    @Operation(summary = "提交试卷", description = "提交答案并获取考试结果")
    public Result<ExamResultVO> submitExam(@RequestBody ExamSubmitDTO submitDTO) {
        try {
            ExamResultVO result = examService.submitExam(submitDTO);
            if (result.isSuccess()) {
                return Result.success(result.getMessage(), result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("提交试卷失败:", e);
            return Result.error("提交试卷失败: " + e.getMessage());
        }
    }

    /**
     * 获取考试记录
     */
    @GetMapping("/exam/records")
    @Operation(summary = "考试记录", description = "获取用户的考试记录")
    public Result<List<com.volunteer.vo.CourseExamRecordVO>> getExamRecords(
            @Parameter(description = "课程ID(可选)") @RequestParam(required = false) Long courseId) {
        try {
            List<com.volunteer.vo.CourseExamRecordVO> records = examService.getExamRecords(courseId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取考试记录失败:", e);
            return Result.error("获取考试记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "学习统计", description = "获取当前用户的学习进度统计数据")
    public Result<com.volunteer.vo.TrainingStatsVO> getTrainingStats() {
        return Result.success(examService.getTrainingStats());
    }

    /**
     * 获取继续学习的课程
     */
    @GetMapping("/resume")
    @Operation(summary = "继续学习", description = "获取用户上次参加学习/考试的课程")
    public Result<Course> getResumeCourse() {
        return Result.success(examService.getResumeCourse());
    }

    /**
     * 获取必修课程列表
     */
    @GetMapping("/mandatory")
    @Operation(summary = "必修课程", description = "获取岗前必修课程列表")
    public Result<List<Course>> getMandatoryCourses() {
        return Result.success(examService.getMandatoryCourses());
    }

    /**
     * 获取待考提醒列表
     */
    @GetMapping("/exams/pending")
    @Operation(summary = "待考提醒", description = "获取已学完但未通过考试的课程提醒")
    public Result<List<com.volunteer.vo.CourseExamRecordVO>> getPendingExams() {
        return Result.success(examService.getPendingExams());
    }

    /**
     * 检查是否已通过考试
     */
    @GetMapping("/exam/passed")
    @Operation(summary = "是否已通过", description = "检查用户是否已通过某课程考试")
    public Result<Boolean> hasPassedExam(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        if (courseId == null) {
            return Result.success(false);
        }
        try {
            boolean passed = examService.hasPassedExam(courseId);
            return Result.success(passed);
        } catch (Exception e) {
            log.error("检查考试状态失败:", e);
            return Result.success(false);
        }
    }
}

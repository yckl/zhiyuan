package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.CourseExamService;
import com.volunteer.vo.ExamPaperVO;
import com.volunteer.vo.ExamResultVO;
import com.volunteer.vo.ExamSubmitDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 培训考试服务实现
 * 核心逻辑：生成试卷、提交试卷、计分结算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseExamServiceImpl implements CourseExamService {

    private final CourseMapper courseMapper;
    private final CourseQuestionMapper questionMapper;
    private final CourseExamRecordMapper examRecordMapper;
    private final VolunteerMapper volunteerMapper;
    private final CourseProgressMapper courseProgressMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 每题分值
    private static final int SCORE_PER_QUESTION = 10;

    @Override
    public List<Course> getCourseList(String category) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1)
                .eq(Course::getIsDeleted, 0);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Course::getCategory, category);
        }
        wrapper.orderByAsc(Course::getSortOrder).orderByDesc(Course::getCreateTime);
        return courseMapper.selectList(wrapper);
    }

    @Override
    public Course getCourseDetail(Long courseId) {
        return courseMapper.selectById(courseId);
    }

    /**
     * 生成试卷
     * 使用全局共享的3道标准题目（courseId=0），固定顺序、固定选项
     */
    @Override
    public ExamPaperVO generateExamPaper(Long courseId, int questionCount) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 获取全局共享题目（3道标准题）
        List<CourseQuestion> questions = questionMapper.getSharedQuestions();
        if (questions.isEmpty()) {
            throw new RuntimeException("暂无题目，请联系管理员");
        }

        // 构建试卷VO（不含答案）
        ExamPaperVO paper = new ExamPaperVO();
        paper.setCourseId(courseId);
        paper.setCourseName(course.getTitle());
        paper.setTotalScore(questions.size() * SCORE_PER_QUESTION);
        paper.setPassScore(CourseExamRecord.PASS_SCORE);
        paper.setTimeLimit(10); // 10分钟

        List<ExamPaperVO.QuestionVO> questionVOs = new ArrayList<>();
        for (CourseQuestion q : questions) {
            ExamPaperVO.QuestionVO vo = new ExamPaperVO.QuestionVO();
            vo.setId(q.getId());
            vo.setQuestionType(q.getQuestionType());
            vo.setContent(q.getContent());
            vo.setScore(SCORE_PER_QUESTION);
            vo.setCorrectAnswer(q.getCorrectAnswer());
            vo.setExplanation(q.getExplanation());

            // 解析选项JSON
            try {
                if (q.getOptions() != null) {
                    List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {
                    });
                    vo.setOptions(options);
                }
            } catch (Exception e) {
                log.warn("解析选项失败: {}", e.getMessage());
                vo.setOptions(new ArrayList<>());
            }

            questionVOs.add(vo);
        }
        paper.setQuestions(questionVOs);

        log.info("生成试卷: courseId={}, 题目数={}", courseId, questions.size());
        return paper;
    }

    /**
     * 提交试卷
     * 计算得分，判断是否通过，发放积分（禁止重复刷分）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResultVO submitExam(ExamSubmitDTO submitDTO) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return ExamResultVO.fail("请先登录");
        }

        Long courseId = submitDTO.getCourseId();
        List<ExamSubmitDTO.AnswerItem> answers = submitDTO.getAnswers();

        if (courseId == null || answers == null || answers.isEmpty()) {
            return ExamResultVO.fail("提交数据不完整");
        }

        // 检查是否已通过（禁止重复刷分）
        CourseExamRecord passedRecord = examRecordMapper.findPassedRecord(volunteer.getId(), courseId);
        boolean alreadyPassed = passedRecord != null;

        // 获取题目ID列表
        List<Long> questionIds = answers.stream()
                .map(ExamSubmitDTO.AnswerItem::getQuestionId)
                .collect(Collectors.toList());

        // 查询题目（含正确答案）
        List<CourseQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, CourseQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(CourseQuestion::getId, q -> q));

        // 计算得分
        int totalScore = 0;
        int correctCount = 0;
        List<ExamResultVO.AnswerDetail> details = new ArrayList<>();

        for (ExamSubmitDTO.AnswerItem answer : answers) {
            CourseQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null)
                continue;

            ExamResultVO.AnswerDetail detail = new ExamResultVO.AnswerDetail();
            detail.setQuestionId(question.getId());
            detail.setContent(question.getContent());
            detail.setUserAnswer(answer.getUserAnswer());
            detail.setCorrectAnswer(question.getCorrectAnswer());
            detail.setExplanation(question.getExplanation());

            // 比对答案（忽略大小写和顺序）
            boolean isCorrect = compareAnswer(answer.getUserAnswer(), question.getCorrectAnswer());
            detail.setCorrect(isCorrect);

            if (isCorrect) {
                correctCount++;
                totalScore += SCORE_PER_QUESTION;
                detail.setScore(SCORE_PER_QUESTION);
            } else {
                detail.setScore(0);
            }

            details.add(detail);
        }

        int totalQuestions = answers.size();
        int fullScore = totalQuestions * SCORE_PER_QUESTION;
        boolean passed = totalScore >= CourseExamRecord.PASS_SCORE;

        // 计算用时
        int timeSpent = 0;
        if (submitDTO.getStartTime() != null) {
            timeSpent = (int) ((System.currentTimeMillis() - submitDTO.getStartTime()) / 1000);
        }

        // 积分奖励（已根据需求移除）
        int pointsReward = 0;
        boolean firstPass = false;
        if (passed && !alreadyPassed) {
            firstPass = true;
            log.info("用户 {} 首次通过课程 {} 考试, 课程已标记为完成", volunteer.getId(), courseId);

            // 更新学习进度为 100%
            updateCourseProgressToCompleted(volunteer.getId(), courseId);
        }

        // 保存考试记录
        CourseExamRecord record = new CourseExamRecord();
        record.setVolunteerId(volunteer.getId());
        record.setCourseId(courseId);
        record.setScore(totalScore);
        record.setTotalScore(fullScore);
        record.setCorrectCount(correctCount);
        record.setTotalCount(totalQuestions);
        record.setPassed(passed ? 1 : 0);
        record.setPointsReward(pointsReward);
        record.setTimeSpent(timeSpent);
        record.setSubmitTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);

        // 保存答题详情
        try {
            record.setAnswers(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            log.warn("序列化答题详情失败: {}", e.getMessage());
        }

        examRecordMapper.insert(record);

        // 构建结果
        ExamResultVO result = ExamResultVO.success(totalScore, fullScore, passed, pointsReward);
        result.setCorrectCount(correctCount);
        result.setTotalCount(totalQuestions);
        result.setTimeSpent(timeSpent);
        result.setFirstPass(firstPass);
        result.setDetails(details);

        if (alreadyPassed && passed) {
            result.setMessage("考试通过！(已通过过，不再发放积分)");
        }

        log.info("用户 {} 提交课程 {} 考试, 得分 {}/{}, {}",
                volunteer.getId(), courseId, totalScore, fullScore, passed ? "通过" : "未通过");

        return result;
    }

    @Override
    public List<com.volunteer.vo.CourseExamRecordVO> getExamRecords(Long courseId) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return new ArrayList<>();
        }
        return examRecordMapper.findAllRecordsVO(volunteer.getId(), courseId);
    }

    @Override
    public boolean hasPassedExam(Long courseId) {
        if (courseId == null) {
            return false;
        }
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return false;
        }
        CourseExamRecord record = examRecordMapper.findPassedRecord(volunteer.getId(), courseId);
        return record != null;
    }

    @Override
    public com.volunteer.vo.TrainingStatsVO getTrainingStats() {
        try {
            Volunteer volunteer = getCurrentVolunteer();
            com.volunteer.vo.TrainingStatsVO stats = new com.volunteer.vo.TrainingStatsVO();
            if (volunteer == null) {
                stats.setTotalHours(java.math.BigDecimal.ZERO);
                stats.setCompletedCount(0);
                stats.setCertificateCount(0);
                stats.setAverageScore(0);
                return stats;
            }

            // 累计学分/学时
            java.math.BigDecimal totalHours = examRecordMapper.sumPassedHours(volunteer.getId());
            stats.setTotalHours(totalHours != null ? totalHours : java.math.BigDecimal.ZERO);

            // 已完成课程数
            Long completedCount = examRecordMapper.selectCount(new LambdaQueryWrapper<CourseExamRecord>()
                    .eq(CourseExamRecord::getVolunteerId, volunteer.getId())
                    .eq(CourseExamRecord::getPassed, 1)
                    .eq(CourseExamRecord::getIsDeleted, 0));
            stats.setCompletedCount(completedCount != null ? completedCount.intValue() : 0);

            // 获得证书数 (目前暂定每2门1证)
            stats.setCertificateCount(stats.getCompletedCount() / 2);

            // 考试均分
            List<CourseExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<CourseExamRecord>()
                    .eq(CourseExamRecord::getVolunteerId, volunteer.getId())
                    .eq(CourseExamRecord::getIsDeleted, 0));
            if (records != null && !records.isEmpty()) {
                double avg = records.stream()
                        .filter(r -> r != null && r.getScore() != null)
                        .mapToInt(CourseExamRecord::getScore)
                        .average()
                        .orElse(0);
                stats.setAverageScore((int) avg);
            } else {
                stats.setAverageScore(0);
            }

            return stats;
        } catch (Exception e) {
            log.error("计算学习统计失败:", e);
            com.volunteer.vo.TrainingStatsVO failStats = new com.volunteer.vo.TrainingStatsVO();
            failStats.setTotalHours(java.math.BigDecimal.ZERO);
            failStats.setCompletedCount(0);
            failStats.setCertificateCount(0);
            failStats.setAverageScore(0);
            return failStats;
        }
    }

    @Override
    public Map<String, Object> getResumeCourse() {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null)
            return null;

        Course course = null;
        CourseExamRecord latest = examRecordMapper.findLatestRecord(volunteer.getId());
        if (latest != null) {
            course = courseMapper.selectById(latest.getCourseId());
        }

        if (course == null) {
            // 如果没有考试记录，返回第一条必修课
            course = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getIsRequired, 1)
                    .eq(Course::getStatus, 1)
                    .last("LIMIT 1"));
        }

        if (course == null)
            return null;

        // 组装带进度的结果
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", course.getId());
        map.put("title", course.getTitle());
        map.put("coverImage", course.getCoverImage());
        map.put("instructor", course.getInstructor());

        // 查询通过状态和进度
        boolean passed = hasPassedExam(course.getId());
        map.put("passed", passed);

        int progress = 0;
        if (passed) {
            progress = 100;
        } else {
            CourseProgress cp = courseProgressMapper.selectOne(new LambdaQueryWrapper<CourseProgress>()
                    .eq(CourseProgress::getVolunteerId, volunteer.getId())
                    .eq(CourseProgress::getCourseId, course.getId())
                    .eq(CourseProgress::getIsDeleted, 0));
            progress = (cp != null && cp.getProgress() != null) ? cp.getProgress() : 0;
        }
        map.put("progress", progress);

        return map;
    }

    /**
     * 当用户通过考试时，强制更新学习进度为 100%
     */
    private void updateCourseProgressToCompleted(Long volunteerId, Long courseId) {
        try {
            CourseProgress progress = courseProgressMapper.selectOne(new LambdaQueryWrapper<CourseProgress>()
                    .eq(CourseProgress::getVolunteerId, volunteerId)
                    .eq(CourseProgress::getCourseId, courseId));

            if (progress == null) {
                progress = new CourseProgress();
                progress.setVolunteerId(volunteerId);
                progress.setCourseId(courseId);
                progress.setProgress(100);
                progress.setIsCompleted(1);
                progress.setCompletedTime(LocalDateTime.now());
                progress.setCreateTime(LocalDateTime.now());
                progress.setUpdateTime(LocalDateTime.now());
                progress.setIsDeleted(0);
                courseProgressMapper.insert(progress);
            } else {
                progress.setProgress(100);
                progress.setIsCompleted(1);
                progress.setCompletedTime(LocalDateTime.now());
                progress.setUpdateTime(LocalDateTime.now());
                courseProgressMapper.updateById(progress);
            }
        } catch (Exception e) {
            log.error("自动更新课程进度失败: volunteerId={}, courseId={}", volunteerId, courseId, e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllCoursesWithStatus() {
        // 1. 查询所有上架课程
        List<Course> allCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getSortOrder)
                .orderByDesc(Course::getCreateTime));

        // 2. 获取当前用户的通过记录和学习进度
        Volunteer volunteer = getCurrentVolunteer();
        Set<Long> passedCourseIds = new HashSet<>();
        Map<Long, Integer> progressMap = new HashMap<>();

        if (volunteer != null) {
            // 查询通过的课程
            List<CourseExamRecord> passedRecords = examRecordMapper.selectList(
                    new LambdaQueryWrapper<CourseExamRecord>()
                            .eq(CourseExamRecord::getVolunteerId, volunteer.getId())
                            .eq(CourseExamRecord::getPassed, 1)
                            .eq(CourseExamRecord::getIsDeleted, 0));
            for (CourseExamRecord r : passedRecords) {
                passedCourseIds.add(r.getCourseId());
            }

            // 查询学习进度
            List<CourseProgress> progressList = courseProgressMapper.selectList(
                    new LambdaQueryWrapper<CourseProgress>()
                            .eq(CourseProgress::getVolunteerId, volunteer.getId())
                            .eq(CourseProgress::getIsDeleted, 0));
            for (CourseProgress p : progressList) {
                progressMap.put(p.getCourseId(), p.getProgress() != null ? p.getProgress() : 0);
            }
        }

        // 3. 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : allCourses) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("title", c.getTitle());
            map.put("coverImage", c.getCoverImage());
            map.put("category", c.getCategory());
            map.put("instructor", c.getInstructor());
            map.put("summary", c.getSummary());
            map.put("creditHours", c.getCreditHours());
            map.put("difficulty", c.getDifficulty());
            map.put("viewCount", c.getViewCount());
            map.put("duration", c.getDuration());

            // courseType: 基于 isRequired 字段，null 时用 id % 3 确定性分配
            boolean isReq;
            if (c.getIsRequired() != null) {
                isReq = c.getIsRequired() == 1;
            } else {
                isReq = c.getId() % 3 != 0; // 约 2/3 必修，1/3 选修
            }
            map.put("courseType", isReq ? "required" : "elective");
            map.put("isRequired", isReq ? 1 : 0);

            // passed: 是否通过考试
            boolean passed = passedCourseIds.contains(c.getId());
            map.put("passed", passed);

            // progress: 学习进度百分比（通过的课程直接100%）
            int progress = passed ? 100 : progressMap.getOrDefault(c.getId(), 0);
            map.put("progress", progress);

            result.add(map);
        }

        return result;
    }

    @Override
    public List<com.volunteer.vo.CourseExamRecordVO> getPendingExams() {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null)
            return new ArrayList<>();

        // 查找参加过但未通过的记录，且当前没有通过记录的课程
        return examRecordMapper.findPendingExamsVO(volunteer.getId(), 3);
    }

    /**
     * 比对答案（忽略大小写，多选题忽略顺序）
     */
    private boolean compareAnswer(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        // 转大写并去除空格
        String user = userAnswer.toUpperCase().replaceAll("\\s+", "");
        String correct = correctAnswer.toUpperCase().replaceAll("\\s+", "");

        // 对于多选题，排序后比较
        if (user.length() > 1 || correct.length() > 1) {
            char[] userChars = user.toCharArray();
            char[] correctChars = correct.toCharArray();
            Arrays.sort(userChars);
            Arrays.sort(correctChars);
            return Arrays.equals(userChars, correctChars);
        }

        return user.equals(correct);
    }

    /**
     * 获取当前志愿者
     */
    private Volunteer getCurrentVolunteer() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
            return null;

        return volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
    }
}

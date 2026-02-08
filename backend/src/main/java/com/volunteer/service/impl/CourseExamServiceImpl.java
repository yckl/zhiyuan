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
    private final PointsRecordMapper pointsRecordMapper;
    private final CourseProgressMapper courseProgressMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 默认抽题数量
    private static final int DEFAULT_QUESTION_COUNT = 10;
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
     * 随机抽取题目，返回不含正确答案的试卷
     * 前置条件：必须完成课程学习（进度>=80%）
     */
    @Override
    public ExamPaperVO generateExamPaper(Long courseId, int questionCount) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查课程学习进度（防止刷课作弊）
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer != null) {
            CourseProgress progress = courseProgressMapper.findByVolunteerAndCourse(volunteer.getId(), courseId);
            if (progress == null || progress.getProgress() < CourseProgress.MIN_PROGRESS_FOR_EXAM) {
                int currentProgress = progress != null ? progress.getProgress() : 0;
                throw new RuntimeException(String.format(
                        "请先完成课程学习（当前进度: %d%%，需要达到 %d%%）",
                        currentProgress, CourseProgress.MIN_PROGRESS_FOR_EXAM));
            }
        }

        if (questionCount <= 0) {
            questionCount = DEFAULT_QUESTION_COUNT;
        }

        // 随机抽取题目
        List<CourseQuestion> questions = questionMapper.getRandomQuestions(courseId, questionCount);
        if (questions.isEmpty()) {
            throw new RuntimeException("该课程暂无题目");
        }

        // 构建试卷VO（不含答案）
        ExamPaperVO paper = new ExamPaperVO();
        paper.setCourseId(courseId);
        paper.setCourseName(course.getTitle());
        paper.setTotalScore(questions.size() * SCORE_PER_QUESTION);
        paper.setPassScore(CourseExamRecord.PASS_SCORE);
        paper.setTimeLimit(30); // 30分钟

        List<ExamPaperVO.QuestionVO> questionVOs = new ArrayList<>();
        for (CourseQuestion q : questions) {
            ExamPaperVO.QuestionVO vo = new ExamPaperVO.QuestionVO();
            vo.setId(q.getId());
            vo.setQuestionType(q.getQuestionType());
            vo.setContent(q.getContent());
            vo.setScore(SCORE_PER_QUESTION);

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

        // 积分奖励（仅首次通过发放）
        int pointsReward = 0;
        boolean firstPass = false;
        if (passed && !alreadyPassed) {
            pointsReward = CourseExamRecord.PASS_REWARD_POINTS;
            firstPass = true;
            addPoints(volunteer, pointsReward, "课程考试通过奖励");
            log.info("用户 {} 首次通过课程 {} 考试, 奖励 {} 积分", volunteer.getId(), courseId, pointsReward);
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
    public Course getResumeCourse() {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null)
            return null;

        CourseExamRecord latest = examRecordMapper.findLatestRecord(volunteer.getId());
        if (latest != null) {
            return courseMapper.selectById(latest.getCourseId());
        }

        // 如果没有考试记录，返回第一条必修课
        return courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsRequired, 1)
                .eq(Course::getStatus, 1)
                .last("LIMIT 1"));
    }

    @Override
    public List<Course> getMandatoryCourses() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsRequired, 1)
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getSortOrder)
                .last("LIMIT 4"));
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
     * 增加用户积分
     */
    private void addPoints(Volunteer volunteer, int points, String description) {
        int newBalance = (volunteer.getAvailablePoints() != null ? volunteer.getAvailablePoints() : 0) + points;
        int newTotal = (volunteer.getTotalPoints() != null ? volunteer.getTotalPoints() : 0) + points;

        volunteer.setAvailablePoints(newBalance);
        volunteer.setTotalPoints(newTotal);
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        PointsRecord record = new PointsRecord();
        record.setVolunteerId(volunteer.getId());
        record.setPoints(points);
        record.setBalance(newBalance);
        record.setType(PointsRecord.Type.ACTIVITY);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        pointsRecordMapper.insert(record);
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

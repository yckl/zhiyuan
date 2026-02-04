package com.volunteer.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试结果 VO
 */
@Data
public class ExamResultVO {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 得分 */
    private int score;

    /** 总分 */
    private int totalScore;

    /** 正确题数 */
    private int correctCount;

    /** 总题数 */
    private int totalCount;

    /** 是否通过 */
    private boolean passed;

    /** 获得积分 */
    private int pointsReward;

    /** 用时(秒) */
    private int timeSpent;

    /** 是否首次通过 */
    private boolean firstPass;

    /** 答题详情（包含正确答案供复习） */
    private List<AnswerDetail> details;

    @Data
    public static class AnswerDetail {
        /** 题目ID */
        private Long questionId;
        /** 题目内容 */
        private String content;
        /** 用户答案 */
        private String userAnswer;
        /** 正确答案 */
        private String correctAnswer;
        /** 是否正确 */
        private boolean correct;
        /** 得分 */
        private int score;
        /** 解析 */
        private String explanation;
    }

    public static ExamResultVO success(int score, int totalScore, boolean passed, int pointsReward) {
        ExamResultVO vo = new ExamResultVO();
        vo.setSuccess(true);
        vo.setScore(score);
        vo.setTotalScore(totalScore);
        vo.setPassed(passed);
        vo.setPointsReward(pointsReward);
        vo.setMessage(passed ? "恭喜通过考试！" : "很遗憾，未通过考试");
        return vo;
    }

    public static ExamResultVO fail(String message) {
        ExamResultVO vo = new ExamResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}

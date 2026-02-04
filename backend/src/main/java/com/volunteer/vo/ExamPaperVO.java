package com.volunteer.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试试卷 VO（不含正确答案）
 */
@Data
public class ExamPaperVO {

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 题目列表 */
    private List<QuestionVO> questions;

    /** 总分 */
    private int totalScore;

    /** 时间限制(分钟) */
    private int timeLimit;

    /** 及格分数 */
    private int passScore;

    @Data
    public static class QuestionVO {
        /** 题目ID */
        private Long id;

        /** 题目类型:1-单选,2-多选,3-判断 */
        private Integer questionType;

        /** 题目内容 */
        private String content;

        /** 选项列表 */
        private List<String> options;

        /** 分值 */
        private Integer score;

        // 注意：不包含 correctAnswer
    }
}

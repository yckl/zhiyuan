package com.volunteer.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试提交 DTO
 */
@Data
public class ExamSubmitDTO {

    /** 课程ID */
    private Long courseId;

    /** 答题列表 */
    private List<AnswerItem> answers;

    /** 开始时间戳 */
    private Long startTime;

    @Data
    public static class AnswerItem {
        /** 题目ID */
        private Long questionId;

        /** 用户答案(如A,AB,TRUE) */
        private String userAnswer;
    }
}

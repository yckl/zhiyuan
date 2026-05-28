package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程题目实体
 * 对应表: course_question
 */
@Data
@Accessors(chain = true)
@TableName("course_question")
public class CourseQuestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属课程ID */
    private Long courseId;

    /** 类型:1-单选,2-多选,3-判断 */
    private Integer questionType;

    /** 题目内容 */
    private String content;

    /** 选项(JSON数组) */
    private String options;

    /** 正确答案(如A,AB) */
    private String correctAnswer;

    /** 分值 */
    private Integer score;

    /** 难度:1-简单,2-中等,3-困难 */
    private Integer difficulty;

    /** 答案解析 */
    private String explanation;

    /** 排序 */
    private Integer sortOrder;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 题目类型常量 */
    public static class QuestionType {
        public static final int SINGLE = 1; // 单选
        public static final int MULTIPLE = 2; // 多选
        public static final int JUDGE = 3; // 判断
    }
}

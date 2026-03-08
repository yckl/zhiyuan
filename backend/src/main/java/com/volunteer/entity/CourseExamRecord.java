package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程考试记录实体
 * 对应表: course_exam_record
 */
@Data
@Accessors(chain = true)
@TableName("course_exam_record")
public class CourseExamRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 课程ID */
    private Long courseId;

    /** 得分 */
    private Integer score;

    /** 总分 */
    private Integer totalScore;

    /** 正确题数 */
    private Integer correctCount;

    /** 总题数 */
    private Integer totalCount;

    /** 是否通过:0-否,1-是 */
    private Integer passed;

    /** 获得积分 */
    private Integer pointsReward;

    /** 答题详情(JSON) */
    private String answers;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 用时(秒) */
    private Integer timeSpent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 及格分数线（3题×10分=30，需全部答对） */
    public static final int PASS_SCORE = 30;

    /** 通过奖励积分 */
    public static final int PASS_REWARD_POINTS = 20;
}

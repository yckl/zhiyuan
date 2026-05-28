package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动评价实体类
 */
@Data
@Accessors(chain = true)
@TableName("activity_review")
public class ActivityReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联活动ID */
    private Long activityId;

    /** 评价人ID (志愿者) */
    private Long volunteerId;

    /** 被评价的组织者ID */
    private Long organizerId;

    /** 评分 (1-5星) */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 组织者回复内容 */
    private String replyContent;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 状态 (0=正常显示, 1=申诉中/隐藏, 2=已屏蔽) */
    private Integer status;

    /** 是否匿名 (0-否, 1-是) */
    private Integer isAnonymous;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

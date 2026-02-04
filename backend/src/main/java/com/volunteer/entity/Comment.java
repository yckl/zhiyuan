package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 对应表: comment
 */
@Data
@Accessors(chain = true)
@TableName("comment")
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 评论用户ID */
    private Long userId;

    /** 目标类型：ACTIVITY,EXPERIENCE,COURSE */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 父评论ID（0为顶级评论） */
    private Long parentId;

    /** 回复的评论ID */
    private Long replyToId;

    /** 回复的用户ID */
    private Long replyToUserId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态：0-隐藏，1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // ========== 目标类型常量 ==========
    public static final String TARGET_ACTIVITY = "ACTIVITY";
    public static final String TARGET_EXPERIENCE = "EXPERIENCE";
    public static final String TARGET_COURSE = "COURSE";

    // ========== 状态常量 ==========
    public static final Integer STATUS_HIDDEN = 0;
    public static final Integer STATUS_NORMAL = 1;
}

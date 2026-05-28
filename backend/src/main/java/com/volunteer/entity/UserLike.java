package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞实体
 */
@Data
@TableName("user_like")
public class UserLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标类型：EXPERIENCE,ACTIVITY,COMMENT
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 目标类型常量
    public static final String TARGET_TYPE_EXPERIENCE = "EXPERIENCE";
    public static final String TARGET_TYPE_ACTIVITY = "ACTIVITY";
    public static final String TARGET_TYPE_COMMENT = "COMMENT";
}

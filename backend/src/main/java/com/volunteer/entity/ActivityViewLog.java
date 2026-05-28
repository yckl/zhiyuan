package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动浏览记录实体
 * 对应表: activity_view_log
 */
@Data
@Accessors(chain = true)
@TableName("activity_view_log")
public class ActivityViewLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 活动ID */
    private Long activityId;

    /** 浏览时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime viewTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

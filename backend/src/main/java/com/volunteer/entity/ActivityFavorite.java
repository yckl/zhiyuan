package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动收藏实体类
 * 对应表: activity_favorite
 */
@Data
@Accessors(chain = true)
@TableName("activity_favorite")
public class ActivityFavorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 活动ID */
    private Long activityId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

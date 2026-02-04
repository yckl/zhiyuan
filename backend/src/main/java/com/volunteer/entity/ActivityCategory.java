package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动分类实体类
 * 对应表: activity_category
 */
@Data
@Accessors(chain = true)
@TableName("activity_category")
public class ActivityCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类图标 */
    private String icon;

    /** 排序值 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // ========== 状态常量 ==========
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;
}

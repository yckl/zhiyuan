package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 * 对应表: collection
 * 支持收藏活动、课程、公告等多种类型
 */
@Data
@Accessors(chain = true)
@TableName("collection")
@Alias("UserCollection")
public class Collection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标类型：ACTIVITY-活动, COURSE-课程, NOTICE-公告, EXPERIENCE-心得 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /**
     * 目标类型枚举
     */
    public static class TargetType {
        public static final String ACTIVITY = "ACTIVITY";
        public static final String COURSE = "COURSE";
        public static final String NOTICE = "NOTICE";
        public static final String EXPERIENCE = "EXPERIENCE";
    }
}

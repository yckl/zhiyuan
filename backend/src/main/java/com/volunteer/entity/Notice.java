package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告/通知实体
 */
@Data
@TableName("notice")
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型：NOTICE-公告, SYSTEM-系统通知, ACTIVITY-活动通知 */
    private String type;

    /** 状态：0-草稿，1-已发布 */
    private Integer status;

    /** 置顶：0-否，1-是 */
    private Integer isTop;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建者ID */
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

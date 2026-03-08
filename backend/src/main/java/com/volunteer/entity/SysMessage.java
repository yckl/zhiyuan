package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内信实体类
 */
@Data
@Accessors(chain = true)
@TableName("sys_message")
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 接收者ID */
    private Long receiverId;

    /** 发送者ID */
    private Long senderId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型：SYSTEM-系统, URGENT-紧急, NOTICE-公告, ACTIVITY-活动 */
    private String type;

    /** 是否已读：0-未读, 1-已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // 类型常量
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_URGENT = "URGENT";
    public static final String TYPE_NOTICE = "NOTICE";
    public static final String TYPE_ACTIVITY = "ACTIVITY";
    public static final String TYPE_MALL = "MALL";
    public static final String TYPE_INTERACTION = "INTERACTION";
}

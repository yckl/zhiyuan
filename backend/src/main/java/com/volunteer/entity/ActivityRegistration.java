package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动报名实体类
 * 对应表: activity_registration
 */
@Data
@Accessors(chain = true)
@TableName("activity_registration")
public class ActivityRegistration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 志愿者ID */
    private Long volunteerId;

    /** 状态：0-已报名，1-已确认，2-已签到，3-已完成，4-已取消，5-缺席 */
    private Integer status;

    /** 签到时间 */
    private LocalDateTime signInTime;

    /** 签退时间 */
    private LocalDateTime signOutTime;

    /** 实际服务时长 */
    private BigDecimal actualHours;

    /** 实际获得积分 */
    private Integer actualPoints;

    /** 备注 */
    private String remark;

    /** 取消原因 */
    private String cancelReason;

    /** 签到二维码 */
    private String qrCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // ========== 状态常量 ==========
    public static final Integer STATUS_REGISTERED = 0; // 已报名
    public static final Integer STATUS_CONFIRMED = 1; // 已确认
    public static final Integer STATUS_SIGNED_IN = 2; // 已签到
    public static final Integer STATUS_COMPLETED = 3; // 已完成
    public static final Integer STATUS_CANCELLED = 4; // 已取消
    public static final Integer STATUS_ABSENT = 5; // 缺席
    public static final Integer STATUS_REJECTED = 6; // 已拒绝
}

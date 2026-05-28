package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录实体
 * 对应表: points_record
 */
@Data
@Accessors(chain = true)
@TableName("points_record")
public class PointsRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 积分变动（正-增加，负-减少） */
    private Integer points;

    /** 变动后余额 */
    private Integer balance;

    /** 类型：ACTIVITY-活动,SIGNIN-签到,EXCHANGE-兑换,LOTTERY-抽奖,ADMIN-管理员调整 */
    private String type;

    /** 关联业务ID */
    private Long bizId;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 类型常量 */
    public static class Type {
        public static final String ACTIVITY = "ACTIVITY";
        public static final String SIGNIN = "SIGNIN";
        public static final String EXCHANGE = "EXCHANGE";
        public static final String LOTTERY = "LOTTERY";
        public static final String ADMIN = "ADMIN";
    }
}

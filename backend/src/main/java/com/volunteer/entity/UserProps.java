package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户道具/背包实体
 * 对应表: user_props
 */
@Data
@Accessors(chain = true)
@TableName("user_props")
public class UserProps implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 道具类型：COUPON-优惠券,CARD_KEY-卡密,BADGE-徽章,GOODS-商品 */
    private String propType;

    /** 关联道具ID（商品ID等） */
    private Long propId;

    /** 道具名称 */
    private String propName;

    /** 道具图片 */
    private String propImage;

    /** 道具内容（卡密等） */
    private String propContent;

    /** 数量 */
    private Integer quantity;

    /** 状态：0-未使用，1-已使用，2-已过期 */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 使用时间 */
    private LocalDateTime useTime;

    /** 来源：EXCHANGE-兑换,LOTTERY-抽奖,ACTIVITY-活动 */
    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 道具类型常量 */
    public static class PropType {
        public static final String COUPON = "COUPON";
        public static final String CARD_KEY = "CARD_KEY";
        public static final String BADGE = "BADGE";
        public static final String GOODS = "GOODS";
    }

    /** 来源常量 */
    public static class Source {
        public static final String EXCHANGE = "EXCHANGE";
        public static final String LOTTERY = "LOTTERY";
        public static final String ACTIVITY = "ACTIVITY";
    }

    /** 状态常量 */
    public static class Status {
        public static final int UNUSED = 0;
        public static final int USED = 1;
        public static final int EXPIRED = 2;
    }
}

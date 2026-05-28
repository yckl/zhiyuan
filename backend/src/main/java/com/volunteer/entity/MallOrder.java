package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分兑换订单实体
 * 对应表: mall_order
 */
@Data
@Accessors(chain = true)
@TableName("mall_order")
public class MallOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 志愿者ID */
    private Long volunteerId;

    /** 商品ID */
    private Long goodsId;

    /** 商品名称（冗余） */
    private String goodsName;

    /** 商品图片（冗余） */
    private String goodsImage;

    /** 兑换数量 */
    private Integer quantity;

    /** 消耗积分 */
    private Integer pointsCost;

    /** 状态：0-待核销，1-已核销，2-已完成，3-已取消 */
    private Integer status;

    /** 取货码/核销码 (6位) */
    private String pickupCode;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 快递公司 */
    private String expressCompany;

    /** 快递单号 */
    private String expressNo;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 订单状态常量 */
    public static class OrderStatus {
        public static final int PENDING = 0; // 待核销/待发货
        public static final int VERIFIED = 1; // 已核销/已发货
        public static final int COMPLETED = 2; // 已完成
        public static final int CANCELLED = 3; // 已取消
    }
}

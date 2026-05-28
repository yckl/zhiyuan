package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分商城商品实体
 * 对应表: mall_goods
 */
@Data
@Accessors(chain = true)
@TableName("mall_goods")
public class MallGoods implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品分类 */
    private String category;

    /** 封面图片 */
    private String coverImage;

    /** 商品图片列表 (JSON) */
    private String images;

    /** 商品描述 */
    private String description;

    /** 积分价格 */
    private Integer pointsPrice;

    /** 原价（元） */
    private BigDecimal originalPrice;

    /** 库存数量（-1为无限） */
    private Integer stock;

    /** 已售数量 */
    private Integer soldCount;

    /** 类型：0-实物，1-虚拟，2-优惠券 */
    private Integer goodsType;

    /** 虚拟商品内容（卡密等） */
    private String virtualContent;

    /** 每人限购（0-不限） */
    private Integer limitPerUser;

    /** 状态：0-下架，1-上架 */
    private Integer status;

    /** 排序值 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 商品类型常量 */
    public static class GoodsType {
        public static final int PHYSICAL = 0; // 实物
        public static final int VIRTUAL = 1; // 虚拟
        public static final int COUPON = 2; // 优惠券
    }
}

package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抽奖奖品实体
 * 对应表: lottery_prize
 */
@Data
@Accessors(chain = true)
@TableName("lottery_prize")
public class LotteryPrize implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 奖品名称 */
    private String name;

    /** 奖品图片 */
    private String image;

    /** 类型：0-积分，1-实物，2-虚拟道具 */
    private Integer prizeType;

    /** 奖品价值（积分数/商品ID） */
    private Integer prizeValue;

    /** 中奖概率（0-1） */
    private BigDecimal probability;

    /** 每日限量（0-不限） */
    private Integer dailyLimit;

    /** 总限量（0-不限） */
    private Integer totalLimit;

    /** 已中奖次数 */
    private Integer wonCount;

    /** 排序（转盘位置） */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 奖品类型常量 */
    public static class PrizeType {
        public static final int POINTS = 0; // 积分
        public static final int PHYSICAL = 1; // 实物
        public static final int VIRTUAL = 2; // 虚拟道具（补签卡等）
    }
}

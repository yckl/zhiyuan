package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽奖记录实体
 * 对应表: lottery_record
 */
@Data
@Accessors(chain = true)
@TableName("lottery_record")
public class LotteryRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 奖品ID */
    private Long prizeId;

    /** 奖品名称 */
    private String prizeName;

    /** 奖品类型 */
    private Integer prizeType;

    /** 奖品价值 */
    private Integer prizeValue;

    /** 消耗积分 */
    private Integer pointsCost;

    /** 是否中奖 */
    private Integer isWon;

    /** 状态：0-待领取，1-已领取，2-已过期 */
    private Integer status;

    /** 领取时间 */
    private LocalDateTime claimTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

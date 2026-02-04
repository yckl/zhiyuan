package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录实体
 * 对应表: signin_record
 */
@Data
@Accessors(chain = true)
@TableName("signin_record")
public class SigninRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 签到日期 */
    private LocalDate signinDate;

    /** 连续签到天数 */
    private Integer continuousDays;

    /** 获得积分 */
    private Integer pointsEarned;

    /** 是否补签: 0-正常签到, 1-补签 */
    @TableField(exist = false)
    private Integer isMakeup;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿者信息实体类
 * 对应表: volunteer
 */
@Data
@Accessors(chain = true)
@TableName("volunteer")
public class Volunteer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 志愿者ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String major;

    /**
     * 班级
     */
    private String className;

    /**
     * 年级
     */
    private String grade;

    /**
     * 累计志愿时长（小时）
     */
    private BigDecimal totalHours;

    /**
     * 累计积分
     */
    private Integer totalPoints;

    /**
     * 可用积分
     */
    private Integer availablePoints;

    /**
     * 志愿者等级
     */
    private Integer level;

    /**
     * 兴趣标签（JSON数组）
     */
    private String interestTags;

    /**
     * 技能特长
     */
    private String skills;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 身份证号（加密存储）
     */
    private String idCard;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    // ========== 性别常量 ==========
    public static final Integer GENDER_UNKNOWN = 0;
    public static final Integer GENDER_MALE = 1;
    public static final Integer GENDER_FEMALE = 2;
}

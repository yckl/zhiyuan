package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织者信息实体类
 * 对应表: organizer
 */
@Data
@Accessors(chain = true)
@TableName("organizer")
public class Organizer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 组织名称 */
    private String orgName;

    /** 组织类型：学院/社团/部门 */
    private String orgType;

    /** 联系人姓名 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 联系邮箱 */
    private String contactEmail;

    /** 组织简介 */
    private String description;

    /** 组织Logo */
    private String logo;

    /** 办公地址 */
    private String address;

    /** 是否认证：0-未认证，1-已认证 */
    private Integer verified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

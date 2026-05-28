package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应表: sys_user
 */
@Data
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（学号/工号/管理员账号）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 角色类型：ADMIN-管理员，VOLUNTEER-志愿者，ORGANIZER-组织者
     */
    private String role;

    /**
     * 账号状态：0-禁用，1-正常，2-锁定
     */
    private Integer status;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

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
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    // ========== 角色常量 ==========
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_VOLUNTEER = "VOLUNTEER";
    public static final String ROLE_ORGANIZER = "ORGANIZER";

    // ========== 状态常量 ==========
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_NORMAL = 1;
    public static final Integer STATUS_LOCKED = 2;
}

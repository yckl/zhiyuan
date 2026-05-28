package com.volunteer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 * 用于返回用户信息，不包含敏感数据（密码）
 */
@Data
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色类型: ADMIN/VOLUNTEER/ORGANIZER")
    private String role;

    @Schema(description = "状态: 0=禁用, 1=正常, 2=锁定")
    private Integer status;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 获取角色显示名称
     */
    public String getRoleName() {
        if (role == null)
            return "未知";
        return switch (role) {
            case "ADMIN" -> "管理员";
            case "VOLUNTEER" -> "志愿者";
            case "ORGANIZER" -> "组织者";
            default -> role;
        };
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusName() {
        if (status == null)
            return "未知";
        return switch (status) {
            case 0 -> "禁用";
            case 1 -> "正常";
            case 2 -> "锁定";
            default -> "未知";
        };
    }
}

package com.volunteer.security;

import com.volunteer.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getRole() : null;
    }

    /**
     * 获取当前系统用户
     */
    public static SysUser getSysUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getSysUser() : null;
    }

    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        return SysUser.ROLE_ADMIN.equals(getRole());
    }

    /**
     * 判断是否是组织者
     */
    public static boolean isOrganizer() {
        return SysUser.ROLE_ORGANIZER.equals(getRole());
    }

    /**
     * 判断是否是志愿者
     */
    public static boolean isVolunteer() {
        return SysUser.ROLE_VOLUNTEER.equals(getRole());
    }
}

package com.volunteer.service;

import com.volunteer.dto.LoginRequest;
import com.volunteer.dto.LoginResponse;
import com.volunteer.dto.VolunteerRegisterRequest;
import com.volunteer.entity.SysUser;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 志愿者注册
     *
     * @param request 注册请求
     * @return 注册的用户
     */
    SysUser registerVolunteer(VolunteerRegisterRequest request);

    /**
     * 获取当前登录用户
     *
     * @return 当前用户
     */
    SysUser getCurrentUser();

    /**
     * 根据用户ID获取用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getUserById(Long userId);
}

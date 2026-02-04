package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.LoginRequest;
import com.volunteer.dto.LoginResponse;
import com.volunteer.dto.VolunteerRegisterRequest;
import com.volunteer.entity.SysUser;
import com.volunteer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录、注册、Token刷新等接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 志愿者注册
     */
    @PostMapping("/register/volunteer")
    @Operation(summary = "志愿者注册", description = "注册新的志愿者账号")
    public Result<Void> registerVolunteer(@Valid @RequestBody VolunteerRegisterRequest request) {
        try {
            authService.registerVolunteer(request);
            return Result.success("注册成功", null);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    public Result<SysUser> getCurrentUser() {
        SysUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized("未登录或Token已过期");
        }
        // 隐藏密码
        currentUser.setPassword(null);
        return Result.success(currentUser);
    }

    /**
     * 检查Token是否有效
     */
    @GetMapping("/check")
    @Operation(summary = "检查Token", description = "检查当前Token是否有效")
    public Result<Boolean> checkToken() {
        SysUser currentUser = authService.getCurrentUser();
        return Result.success(currentUser != null);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    public Result<Void> logout() {
        // JWT是无状态的，客户端删除Token即可
        // 如需服务端控制，可将Token加入黑名单（存Redis）
        return Result.success("退出成功", null);
    }
}

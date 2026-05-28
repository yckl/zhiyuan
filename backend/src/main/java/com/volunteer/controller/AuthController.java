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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private final StringRedisTemplate redisTemplate;
    private final com.volunteer.security.JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String limitKey = "login:limit:" + request.getUsername() + ":" + ip;
        
        try {
            String countStr = redisTemplate.opsForValue().get(limitKey);
            if (countStr != null && Integer.parseInt(countStr) >= 5) {
                return Result.error(429, "连续登录失败次数过多，请15分钟后再试");
            }
        } catch (Exception e) {
            log.warn("Redis服务不可用，跳过防暴力限制校验: {}", e.getMessage());
        }

        try {
            LoginResponse response = authService.login(request);
            try {
                redisTemplate.delete(limitKey);
            } catch (Exception ignored) {}
            return Result.success("登录成功", response);
        } catch (Exception e) {
            try {
                redisTemplate.opsForValue().increment(limitKey);
                redisTemplate.expire(limitKey, 15, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
            // 如果捕获到的是BusinessException等，原样抛出给全局处理器
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("登录异常", e);
        }
    }

    /**
     * 志愿者注册
     */
    @PostMapping("/register/volunteer")
    @Operation(summary = "志愿者注册", description = "注册新的志愿者账号")
    public Result<Void> registerVolunteer(@Valid @RequestBody VolunteerRegisterRequest request) {
        authService.registerVolunteer(request);
        return Result.success("注册成功", null);
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
    @Operation(summary = "退出登录", description = "退出当前登录状态，废除Token")
    public Result<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            if (expirationDate != null) {
                long expireTime = expirationDate.getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    try {
                        redisTemplate.opsForValue().set("jwt:blacklist:" + token, "logout", expireTime, TimeUnit.MILLISECONDS);
                        log.info("Token已加入黑名单，将在{}毫秒后过期释放", expireTime);
                    } catch (Exception e) {
                        log.warn("Redis不可用，放弃Token黑名单记录: {}", e.getMessage());
                    }
                }
            }
        }
        return Result.success("退出成功", null);
    }
}

package com.volunteer.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.common.Result;
import com.volunteer.entity.SysConfig;
import com.volunteer.mapper.SysConfigMapper;
import com.volunteer.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 维护模式拦截器
 * 当系统处于维护模式时，拦截所有非管理员请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceInterceptor implements HandlerInterceptor {

    private final SysConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    // 白名单路径：这些路径在维护模式下仍可访问
    private static final String[] WHITELIST = {
            "/admin/", // 管理员接口
            "/auth/login", // 登录接口
            "/actuator/", // 健康检查
            "/swagger", // API文档
            "/v3/api-docs" // OpenAPI
    };

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        // 检查是否处于维护模式
        if (!isMaintenanceMode()) {
            return true; // 非维护模式，放行
        }

        String uri = request.getRequestURI();

        // 检查白名单
        for (String path : WHITELIST) {
            if (uri.contains(path)) {
                return true; // 白名单路径，放行
            }
        }

        // 检查是否是管理员
        if (SecurityUtils.isAdmin()) {
            return true; // 管理员，放行
        }

        // 非管理员在维护模式下，返回503
        log.info("维护模式拦截请求: {} (用户: {})", uri, SecurityUtils.getUsername());

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(503, "系统维护中，请稍后再试");
        response.getWriter().write(objectMapper.writeValueAsString(result));

        return false;
    }

    /**
     * 检查是否处于维护模式
     */
    private boolean isMaintenanceMode() {
        try {
            SysConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "maintenance_mode"));
            return config != null && "true".equalsIgnoreCase(config.getConfigValue());
        } catch (Exception e) {
            log.warn("读取维护模式配置失败: {}", e.getMessage());
            return false; // 配置读取失败时默认不是维护模式
        }
    }
}

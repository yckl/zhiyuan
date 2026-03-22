package com.volunteer.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.entity.OperationLog;
import com.volunteer.mapper.OperationLogMapper;
import com.volunteer.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 * 自动记录所有Admin Controller的操作
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 切入点：所有Admin开头的Controller
     */
    @Pointcut("execution(* com.volunteer.controller.Admin*.*(..))")
    public void adminControllerPointcut() {
    }

    @Around("adminControllerPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 跳过日志查询本身，避免无限循环
        if (path.contains("/system/logs")) {
            return joinPoint.proceed();
        }

        // 只记录写操作 (POST, PUT, DELETE)
        if ("GET".equalsIgnoreCase(method)) {
            return joinPoint.proceed();
        }

        OperationLog opLog = new OperationLog();
        opLog.setPath(path);
        opLog.setMethod(method);
        opLog.setCreateTime(LocalDateTime.now());
        opLog.setIp(getClientIP(request));

        // 获取操作人信息
        try {
            Long userId = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            String role = SecurityUtils.getRole();
            opLog.setOperatorId(userId);
            opLog.setOperatorName(username);
            opLog.setRole(role);
        } catch (Exception e) {
            opLog.setOperatorName("Unknown");
        }

        // 获取操作描述 (从@Operation注解)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Operation operation = signature.getMethod().getAnnotation(Operation.class);
        if (operation != null) {
            opLog.setOperation(operation.summary());
        } else {
            opLog.setOperation(signature.getMethod().getName());
        }

        // 获取操作类型
        opLog.setOperationType(getOperationType(method, path));

        // 记录参数（限制长度）
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                opLog.setParams(params);
            }
        } catch (Exception e) {
            opLog.setParams("[序列化失败]");
        }

        // 执行原方法
        Object result;
        try {
            result = joinPoint.proceed();
            opLog.setResult("SUCCESS");
        } catch (Exception e) {
            opLog.setResult("FAIL");
            opLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - startTime);

            // 异步保存日志（避免影响主流程）
            try {
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    private String getOperationType(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.contains("add") || path.contains("create"))
                return "CREATE";
            return "CREATE";
        }
        if ("PUT".equalsIgnoreCase(method))
            return "UPDATE";
        if ("DELETE".equalsIgnoreCase(method))
            return "DELETE";
        return "OTHER";
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

package com.volunteer.aspect;

import com.volunteer.common.annotation.CheckSensitive;
import com.volunteer.exception.SensitiveWordException;
import com.volunteer.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 敏感词校验切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SensitiveCheckAspect {

    private final SensitiveWordFilter sensitiveWordFilter;

    @Before("@annotation(checkSensitive)")
    public void doBefore(JoinPoint joinPoint, CheckSensitive checkSensitive) {
        log.info("开始敏感词校验: 方法={}", joinPoint.getSignature().toShortString());
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        String[] fieldsToCheck = checkSensitive.fields();
        for (Object arg : args) {
            if (arg == null)
                continue;

            // 简单对象通常是 DTO
            checkObject(arg, fieldsToCheck);
        }
    }

    private void checkObject(Object obj, String[] fieldsToCheck) {
        Class<?> clazz = obj.getClass();
        log.info("校验对象: {}, 待检字段: {}", clazz.getSimpleName(), Arrays.toString(fieldsToCheck));
        for (String fieldName : fieldsToCheck) {
            try {
                Field field = getField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String text) {
                        if (sensitiveWordFilter.containsSensitiveWord(text)) {
                            log.warn("检测到敏感词拦截: 字段={}, 内容预览={}", fieldName,
                                    text.length() > 20 ? text.substring(0, 20) + "..." : text);
                            throw new SensitiveWordException("您提交的内容包含违规/敏感词汇，请修改后重试");
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                log.error("敏感词校验反射获取字段失败: {}", e.getMessage());
                throw new RuntimeException("系统安全校验异常，请稍后重试");
            }
        }
    }

    private Field getField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}

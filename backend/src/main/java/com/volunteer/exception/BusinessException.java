package com.volunteer.exception;

import lombok.Getter;

/**
 * 自定义业务异常类，用于处理所有受控的系统业务逻辑错误。
 * 防止滥用 RuntimeException 导致无法区分系统错误和业务拦截。
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认业务错误状态码为主流的 400 Bad Request
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

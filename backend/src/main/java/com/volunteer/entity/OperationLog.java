package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应表: operation_log
 */
@Data
@Accessors(chain = true)
@TableName("operation_log")
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人用户名 */
    private String operatorName;

    /** 操作人角色 */
    private String role;

    /** 操作类型：CREATE, UPDATE, DELETE, QUERY, LOGIN, OTHER */
    private String operationType;

    /** 操作描述 */
    private String operation;

    /** 请求路径 */
    private String path;

    /** 请求方法：GET, POST, PUT, DELETE */
    private String method;

    /** 请求参数 (JSON) */
    private String params;

    /** IP地址 */
    private String ip;

    /** 操作结果：SUCCESS, FAIL */
    private String result;

    /** 错误信息（如果失败） */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.OperationLog;
import com.volunteer.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 管理员系统管理 - 操作日志
 */
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@Tag(name = "系统管理", description = "操作日志等系统功能")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOperationLogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/logs")
    @Operation(summary = "查询操作日志", description = "支持按操作人、时间范围筛选")
    public Result<IPage<OperationLog>> listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> query = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime);

        // 按操作人筛选
        if (StringUtils.hasText(operator)) {
            query.like(OperationLog::getOperatorName, operator);
        }

        // 按时间范围筛选
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDate.parse(startTime).atStartOfDay();
            query.ge(OperationLog::getCreateTime, start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDate.parse(endTime).atTime(LocalTime.MAX);
            query.le(OperationLog::getCreateTime, end);
        }

        return Result.success(operationLogMapper.selectPage(pageParam, query));
    }
}

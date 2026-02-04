package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.SysConfig;
import com.volunteer.entity.SysLog;
import com.volunteer.mapper.SysConfigMapper;
import com.volunteer.mapper.SysLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员系统设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
@Tag(name = "管理员-系统设置", description = "系统配置、操作日志")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SysConfigMapper configMapper;
    private final SysLogMapper logMapper;

    @GetMapping("/config")
    @Operation(summary = "系统配置")
    public Result<Map<String, String>> getConfig() {
        List<SysConfig> list = configMapper.selectList(null);
        Map<String, String> config = new HashMap<>();
        // Set defaults if empty
        if (list.isEmpty()) {
            config.put("site_name", "校园志愿者管理系统");
            config.put("site_logo", "");
            config.put("service_points_per_hour", "1");
            config.put("activity_auto_audit", "false");
            config.put("maintenance_mode", "false");
        } else {
            for (SysConfig sc : list) {
                config.put(sc.getConfigKey(), sc.getConfigValue());
            }
        }
        return Result.success(config);
    }

    @PutMapping("/config/update")
    @Operation(summary = "更新配置")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateConfig(@RequestBody Map<String, String> config) {
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue()); // Ensure string

            // Upsert
            SysConfig exist = configMapper
                    .selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (exist != null) {
                exist.setConfigValue(value);
                configMapper.updateById(exist);
            } else {
                SysConfig newConfig = new SysConfig();
                newConfig.setConfigKey(key);
                newConfig.setConfigValue(value);
                configMapper.insert(newConfig);
            }
        }
        return Result.success("更新成功");
    }

    @GetMapping("/logs")
    @Operation(summary = "操作日志")
    public Result<IPage<SysLog>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        Page<SysLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysLog> query = new LambdaQueryWrapper<SysLog>()
                .orderByDesc(SysLog::getCreateTime);

        if (operator != null && !operator.isBlank()) {
            query.like(SysLog::getOperatorName, operator);
        }
        if (startTime != null && !startTime.isBlank()) {
            query.ge(SysLog::getCreateTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isBlank()) {
            query.le(SysLog::getCreateTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }

        return Result.success(logMapper.selectPage(pageParam, query));
    }
}

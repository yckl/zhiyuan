package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.SysConfig;
import com.volunteer.mapper.SysConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共系统控制?
 */
@RestController
@RequestMapping("/system")
@Tag(name = "公共-系统配置", description = "获取系统基础配置（无权限限制）")
@RequiredArgsConstructor
public class SystemController {

    private final SysConfigMapper configMapper;

    @GetMapping("/config")
    @Operation(summary = "获取公共系统配置")
    public Result<Map<String, String>> getConfig() {
        List<SysConfig> list = configMapper.selectList(null);
        Map<String, String> config = new HashMap<>();

        // 默认?
        config.put("site_name", "校园志愿者管理系统");
        config.put("site_logo", "");

        if (!list.isEmpty()) {
            for (SysConfig sc : list) {
                // 仅暴露非敏感的配置?
                if ("site_name".equals(sc.getConfigKey()) || "site_logo".equals(sc.getConfigKey())) {
                    config.put(sc.getConfigKey(), sc.getConfigValue());
                }
            }
        }
        return Result.success(config);
    }
}

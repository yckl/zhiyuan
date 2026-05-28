package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 组织者统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/organizer/statistics")
@RequiredArgsConstructor
@Tag(name = "组织者统计", description = "工作台核心指标、活动进度、最新评价")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrgStatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取组织者工作台核心指标
     */
    @GetMapping("/core")
    @Operation(summary = "核心指标", description = "获取待审核、进行中活动、累计人次、评分")
    public Result<Map<String, Object>> getCoreStatistics() {
        try {
            Long userId = SecurityUtils.getUserId();
            Map<String, Object> data = statisticsService.getOrgCoreStatistics(userId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取组织者核心统计失败:", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 获取进行中的活动列表
     */
    @GetMapping("/activities/active")
    @Operation(summary = "进行中活动", description = "获取当前组织者正在进行的活动列表")
    public Result<List<Map<String, Object>>> getActiveActivities() {
        try {
            Long userId = SecurityUtils.getUserId();
            List<Map<String, Object>> data = statisticsService.getOrgActiveActivities(userId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取进行中活动失败:", e);
            return Result.error("获取活动数据失败");
        }
    }

    /**
     * 获取最新评价列表
     */
    @GetMapping("/reviews/latest")
    @Operation(summary = "最新评价", description = "获取组织者收到的最新 3 条评价")
    public Result<List<Map<String, Object>>> getLatestReviews() {
        try {
            Long userId = SecurityUtils.getUserId();
            List<Map<String, Object>> data = statisticsService.getOrgLatestReviews(userId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取最新评价失败:", e);
            return Result.error("获取评价数据失败");
        }
    }
}

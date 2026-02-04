package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "数据统计", description = "核心指标、分类饼图、学院柱状图")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 首页统计数据
     */
    @GetMapping("/index")
    @Operation(summary = "首页统计", description = "返回首页所需的核心统计数据")
    public Result<Map<String, Object>> getIndexStatistics() {
        try {
            Map<String, Object> core = statisticsService.getCoreStatistics();
            // 转换字段名以匹配前端期望
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("volunteerCount", core.getOrDefault("volunteerCount", 0));
            result.put("activityCount", core.getOrDefault("activityCount", 0));
            result.put("totalServiceHours", core.getOrDefault("totalHours", 0));
            result.put("totalServiceCount", core.getOrDefault("monthlyRegistrations", 0));
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取首页统计失败:", e);
            // 返回默认数据避免前端显示0
            Map<String, Object> fallback = new java.util.HashMap<>();
            fallback.put("volunteerCount", 0);
            fallback.put("activityCount", 0);
            fallback.put("totalServiceHours", 0.0);
            fallback.put("totalServiceCount", 0);
            return Result.success(fallback);
        }
    }

    /**
     * 获取核心指标
     */
    @GetMapping("/core")
    @Operation(summary = "核心指标", description = "返回志愿者数、活动数、总时长、今日报名数")
    public Result<Map<String, Object>> getCoreStatistics() {
        try {
            Map<String, Object> data = statisticsService.getCoreStatistics();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取核心统计失败:", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类统计（饼图数据）
     */
    @GetMapping("/category")
    @Operation(summary = "分类统计", description = "返回活动分类饼图数据")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        try {
            List<Map<String, Object>> data = statisticsService.getCategoryStatistics();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取分类统计失败:", e);
            return Result.error("获取分类统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取学院排行（柱状图数据）
     */
    @GetMapping("/college")
    @Operation(summary = "学院排行", description = "返回Top10活跃学院柱状图数据")
    public Result<Map<String, Object>> getCollegeStatistics() {
        try {
            Map<String, Object> data = statisticsService.getCollegeStatistics();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取学院统计失败:", e);
            return Result.error("获取学院统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取月度趋势
     */
    @GetMapping("/trend")
    @Operation(summary = "月度趋势", description = "返回近6个月报名趋势")
    public Result<List<Map<String, Object>>> getMonthlyTrend() {
        try {
            List<Map<String, Object>> data = statisticsService.getMonthlyTrend();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取月度趋势失败:", e);
            return Result.error("获取月度趋势失败: " + e.getMessage());
        }
    }
}

package com.volunteer.service;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取核心指标
     */
    Map<String, Object> getCoreStatistics();

    /**
     * 获取分类统计（饼图）
     */
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 获取学院排行（柱状图）
     */
    Map<String, Object> getCollegeStatistics();

    /**
     * 获取月度趋势
     */
    List<Map<String, Object>> getMonthlyTrend();

    /**
     * 获取组织者核心指标
     */
    Map<String, Object> getOrgCoreStatistics(Long orgId);

    /**
     * 获取组织者进行中的活动
     */
    List<Map<String, Object>> getOrgActiveActivities(Long orgId);

    /**
     * 获取组织者最新评价
     */
    List<Map<String, Object>> getOrgLatestReviews(Long orgId);
}

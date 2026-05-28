package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计数据")
public class StatisticsVO {

    @Schema(description = "志愿者总数")
    private Long volunteerCount;

    @Schema(description = "活动总数")
    private Long activityCount;

    @Schema(description = "累计志愿时长（小时）")
    private BigDecimal totalServiceHours;

    @Schema(description = "累计服务人次")
    private Long totalServiceCount;

    @Schema(description = "今日新增志愿者")
    private Long todayVolunteerCount;

    @Schema(description = "今日新增活动")
    private Long todayActivityCount;

    @Schema(description = "待审核活动数")
    private Long pendingActivityCount;

    @Schema(description = "进行中活动数")
    private Long ongoingActivityCount;

    @Schema(description = "心得分享数")
    private Long experienceCount;

    @Schema(description = "评论总数")
    private Long commentCount;

    @Schema(description = "近7天活动数据")
    private List<Map<String, Object>> weeklyActivityData;

    @Schema(description = "近7天报名数据")
    private List<Map<String, Object>> weeklyRegistrationData;

    @Schema(description = "活动分类统计")
    private List<Map<String, Object>> categoryStats;

    @Schema(description = "志愿者等级分布")
    private List<Map<String, Object>> levelDistribution;
}

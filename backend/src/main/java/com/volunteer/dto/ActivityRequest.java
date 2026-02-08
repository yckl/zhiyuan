package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动创建/更新请求
 */
@Data
@Schema(description = "活动创建/更新请求")
public class ActivityRequest {

    @Schema(description = "活动ID（更新时必填）")
    private Long id;

    @Schema(description = "活动状态")
    private Integer status;

    @NotBlank(message = "活动标题不能为空")
    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "活动图片列表(JSON)")
    private String images;

    @Schema(description = "活动摘要")
    private String summary;

    @Schema(description = "活动详情")
    private String content;

    @Schema(description = "活动地点")
    private String location;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @NotNull(message = "活动开始时间不能为空")
    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "报名开始时间")
    private LocalDateTime registerStart;

    @Schema(description = "报名截止时间")
    private LocalDateTime registerEnd;

    @Schema(description = "最大参与人数（0-不限）")
    private Integer maxParticipants;

    @Schema(description = "最小开展人数")
    private Integer minParticipants;

    @Schema(description = "志愿时长（小时）")
    private BigDecimal serviceHours;

    @Schema(description = "积分奖励")
    private Integer pointsReward;

    @Schema(description = "参与要求")
    private String requirements;

    @Schema(description = "联系方式")
    private String contactInfo;

    @Schema(description = "活动标签(JSON)")
    private String tags;
}

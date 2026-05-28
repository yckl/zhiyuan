package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动DTO
 */
@Data
@Schema(description = "活动信息")
public class ActivityDTO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "组织者ID")
    private Long organizerId;

    @Schema(description = "组织者名称")
    private String organizerName;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "活动图片列表")
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

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "报名开始时间")
    private LocalDateTime registerStart;

    @Schema(description = "报名截止时间")
    private LocalDateTime registerEnd;

    @Schema(description = "最大参与人数")
    private Integer maxParticipants;

    @Schema(description = "当前报名人数")
    private Integer currentParticipants;

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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "待审核人数")
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer pendingCount;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "是否精选")
    private Integer isFeatured;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "活动标签")
    private String tags;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否推荐")
    private Boolean isRecommended;

    @Schema(description = "是否最近浏览")
    private Boolean isRecentlyViewed;
}

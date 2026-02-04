package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心得DTO
 */
@Data
@Schema(description = "心得信息")
public class ExperienceDTO {

    @Schema(description = "心得ID")
    private Long id;

    @Schema(description = "志愿者ID")
    private Long volunteerId;

    @Schema(description = "志愿者姓名")
    private String volunteerName;

    @Schema(description = "志愿者头像")
    private String volunteerAvatar;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "心得标题")
    private String title;

    @Schema(description = "心得内容")
    private String content;

    @Schema(description = "图片列表")
    private String images;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "是否精选")
    private Integer isFeatured;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

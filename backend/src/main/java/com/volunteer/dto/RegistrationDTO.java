package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报名信息DTO
 */
@Data
@Schema(description = "报名信息")
public class RegistrationDTO {

    @Schema(description = "报名ID")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "活动封面")
    private String activityCoverImage;

    @Schema(description = "活动开始时间")
    private LocalDateTime activityStartTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime activityEndTime;

    @Schema(description = "志愿者ID")
    private Long volunteerId;

    @Schema(description = "志愿者姓名")
    private String volunteerName;

    @Schema(description = "志愿者头像")
    private String volunteerAvatar;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "签到时间")
    private LocalDateTime signInTime;

    @Schema(description = "签退时间")
    private LocalDateTime signOutTime;

    @Schema(description = "实际服务时长")
    private BigDecimal actualHours;

    @Schema(description = "实际获得积分")
    private Integer actualPoints;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "报名时间")
    private LocalDateTime createTime;
}

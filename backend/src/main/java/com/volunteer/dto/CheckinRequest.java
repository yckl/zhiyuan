package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 签到请求 DTO
 */
@Data
@Schema(description = "签到请求参数")
public class CheckinRequest {

    @Schema(description = "活动ID")
    private Long activityId;
}

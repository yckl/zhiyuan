package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报名请求DTO
 */
@Data
@Schema(description = "报名请求")
public class RegistrationRequest {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "备注")
    private String remark;
}

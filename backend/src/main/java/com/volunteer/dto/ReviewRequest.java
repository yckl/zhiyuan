package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价提交请求 DTO
 */
@Data
@Schema(description = "评价提交请求")
public class ReviewRequest {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Long activityId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "评分 (1-5)")
    private Integer score;

    @NotBlank(message = "评价内容不能为空")
    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous = false;
}

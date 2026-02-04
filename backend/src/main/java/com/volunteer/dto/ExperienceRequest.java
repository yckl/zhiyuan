package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 心得请求DTO
 */
@Data
@Schema(description = "心得请求")
public class ExperienceRequest {

    @Schema(description = "心得ID（更新时必填）")
    private Long id;

    @Schema(description = "关联活动ID")
    private Long activityId;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "心得标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "心得内容")
    private String content;

    @Schema(description = "图片列表(JSON)")
    private String images;
}

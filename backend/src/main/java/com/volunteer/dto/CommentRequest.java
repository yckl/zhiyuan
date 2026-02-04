package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论请求DTO
 */
@Data
@Schema(description = "评论请求")
public class CommentRequest {

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID")
    private Long targetId;

    @NotBlank(message = "目标类型不能为空")
    @Schema(description = "目标类型：ACTIVITY/EXPERIENCE")
    private String targetType;

    @Schema(description = "父评论ID（回复时填写）")
    private Long parentId;

    @Schema(description = "回复的评论ID")
    private Long replyToId;

    @Schema(description = "回复的用户ID")
    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容")
    private String content;
}

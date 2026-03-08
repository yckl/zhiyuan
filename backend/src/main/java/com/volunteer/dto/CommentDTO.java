package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论DTO
 */
@Data
@Schema(description = "评论信息")
public class CommentDTO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "回复的评论ID")
    private Long replyToId;

    @Schema(description = "回复的用户ID")
    private Long replyToUserId;

    @Schema(description = "回复的用户名")
    private String replyToUsername;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评分(1-5星)")
    private Integer rating;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子评论列表")
    private List<CommentDTO> children;
}

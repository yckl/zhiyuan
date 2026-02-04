package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.CommentDTO;
import com.volunteer.dto.CommentRequest;
import com.volunteer.entity.Comment;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping
    @Operation(summary = "发表评论", description = "发表评论（可评论活动或心得）")
    public Result<Comment> createComment(@Valid @RequestBody CommentRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Comment comment = commentService.createComment(request, userId);
            return Result.success("评论成功", comment);
        } catch (Exception e) {
            log.error("发表评论失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/list")
    @Operation(summary = "评论列表", description = "获取某对象下的评论列表（树形结构）")
    public Result<List<CommentDTO>> getCommentList(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            List<CommentDTO> comments = commentService.getCommentList(targetType, targetId);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            commentService.deleteComment(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除评论失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{id}")
    @Operation(summary = "点赞评论", description = "给评论点赞")
    public Result<Void> likeComment(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            commentService.likeComment(id, userId);
            return Result.success("点赞成功", null);
        } catch (Exception e) {
            log.error("点赞评论失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

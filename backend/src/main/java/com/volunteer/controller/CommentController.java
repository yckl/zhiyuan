package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.dto.CommentDTO;
import com.volunteer.dto.CommentRequest;
import com.volunteer.entity.Comment;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.CommentService;
import com.volunteer.common.annotation.CheckSensitive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @CheckSensitive(fields = { "content" })
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
     * 获取评论列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "评论列表", description = "获取某对象下的评论列表（树形结构，分页）")
    public Result<Page<CommentDTO>> getCommentList(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<CommentDTO> result = commentService.getCommentList(targetType, targetId, page, size);
            return Result.success(result);
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

    /**
     * 获取平均评分
     */
    @GetMapping("/rating")
    @Operation(summary = "获取平均评分", description = "获取某对象下所有评论的平均评分")
    public Result<Double> getAverageRating(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            Double avg = commentService.getAverageRating(targetType, targetId);
            return Result.success(avg);
        } catch (Exception e) {
            log.error("获取平均评分失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评论绝对总数
     */
    @GetMapping("/count")
    @Operation(summary = "获取总评论数", description = "获取某目标下所有评论的绝对总数（含回复）")
    public Result<Long> getCommentCount(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            Long count = commentService.getCommentCount(targetType, targetId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取评论总数失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成模拟测试数据（隐藏接口）
     */
    @GetMapping("/mockData")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "生成模拟数据", description = "系统自动为所有活动生成海量嵌套评论以供测试使用")
    public Result<String> generateMockComments() {
        try {
            commentService.generateMockComments();
            return Result.success("Mock comments generated successfully");
        } catch (Exception e) {
            log.error("生成模拟评论数据失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

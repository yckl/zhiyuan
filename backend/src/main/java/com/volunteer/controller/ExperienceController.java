package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.dto.ExperienceDTO;
import com.volunteer.dto.ExperienceRequest;
import com.volunteer.entity.Experience;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 心得分享控制器
 */
@Slf4j
@RestController
@RequestMapping("/experience")
@RequiredArgsConstructor
@Tag(name = "心得分享", description = "志愿心得管理")
public class ExperienceController {

    private final ExperienceService experienceService;

    /**
     * 发布心得
     */
    @PostMapping
    @Operation(summary = "发布心得", description = "发布志愿心得")
    public Result<Experience> createExperience(@Valid @RequestBody ExperienceRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Experience experience = experienceService.createExperience(request, userId);
            return Result.success("发布成功", experience);
        } catch (Exception e) {
            log.error("发布心得失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新心得
     */
    @PutMapping
    @Operation(summary = "更新心得", description = "更新志愿心得")
    public Result<Void> updateExperience(@Valid @RequestBody ExperienceRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            experienceService.updateExperience(request, userId);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新心得失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取心得详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "心得详情", description = "获取心得详情")
    public Result<ExperienceDTO> getExperienceDetail(@PathVariable Long id) {
        try {
            ExperienceDTO experience = experienceService.getExperienceDetail(id);
            // 增加浏览次数
            experienceService.incrementViewCount(id);
            return Result.success(experience);
        } catch (Exception e) {
            log.error("获取心得详情失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 心得列表
     */
    @GetMapping("/list")
    @Operation(summary = "心得列表", description = "分页查询心得列表")
    public Result<Page<ExperienceDTO>> listExperiences(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Long volunteerId,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ExperienceDTO> result = experienceService.pageExperiences(page, size, activityId, volunteerId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询心得列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 公开心得列表（无需登录）
     */
    @GetMapping("/public/list")
    @Operation(summary = "公开心得列表", description = "公开的已发布心得列表，无需登录")
    public Result<Page<ExperienceDTO>> publicListExperiences(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 只查询已发布的心得
            Page<ExperienceDTO> result = experienceService.pageExperiences(pageNum, pageSize, null, null, 1);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询公开心得列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞心得
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞心得", description = "点赞或取消点赞心得")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            boolean isLiked = experienceService.toggleLike(id, userId);
            return Result.success(isLiked ? "点赞成功" : "取消点赞成功", isLiked);
        } catch (Exception e) {
            log.error("点赞操作失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/{id}/like/check")
    @Operation(summary = "检查点赞状态", description = "检查当前用户是否已点赞该心得")
    public Result<Boolean> checkLikeStatus(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.success(false);
        }

        try {
            boolean isLiked = experienceService.checkLiked(id, userId);
            return Result.success(isLiked);
        } catch (Exception e) {
            log.error("检查点赞状态失败: {}", e.getMessage());
            return Result.success(false);
        }
    }

    /**
     * 我的心得
     */
    @GetMapping("/my")
    @Operation(summary = "我的心得", description = "获取当前用户发布的心得")
    public Result<Page<ExperienceDTO>> getMyExperiences(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Page<ExperienceDTO> result = experienceService.pageMyExperiences(page, size, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询我的心得失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除心得
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除心得", description = "删除心得")
    public Result<Void> deleteExperience(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            experienceService.deleteExperience(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除心得失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

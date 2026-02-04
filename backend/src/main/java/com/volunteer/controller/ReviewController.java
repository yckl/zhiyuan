package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.common.Result;
import com.volunteer.service.ReviewService;
import com.volunteer.security.SecurityUtils;
import com.volunteer.vo.ReviewStatsVO;
import com.volunteer.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价管理控制器 (组织者端)
 */
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "组织者端服务评价与双向互动")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 获取评价统计看板
     */
    @GetMapping("/stats")
    @Operation(summary = "评价概览统计", description = "获取当前组织者的平均分、好评率、待回复数等统计")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<ReviewStatsVO> getStats() {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }
        return reviewService.getStats(organizerUserId);
    }

    /**
     * 分页获取评价列表
     * 
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param type       筛选类型: ALL(全部), PENDING(待回复), NEGATIVE(差评1-2星)
     * @param activityId 活动ID筛选(可选)
     */
    @GetMapping("/list")
    @Operation(summary = "评价管理列表", description = "分页条件查询评价列表，支持类型筛选")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<IPage<ReviewVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) Long activityId) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }

        // 根据 type 转换为 score 筛选条件
        Integer score = null;
        boolean pendingOnly = false;

        if ("NEGATIVE".equalsIgnoreCase(type)) {
            // 差评: 1-2星，这里用特殊值-1标识
            score = -1;
        } else if ("PENDING".equalsIgnoreCase(type)) {
            pendingOnly = true;
        }

        return reviewService.listReviews(pageNum, pageSize, score, activityId, organizerUserId, pendingOnly);
    }

    /**
     * 回复评价
     */
    @PostMapping("/reply")
    @Operation(summary = "互动回复", description = "回复志愿者的活动评价")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<String> reply(@RequestBody Map<String, Object> params) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }

        Long reviewId = Long.valueOf(params.get("reviewId").toString());
        String content = (String) params.get("content");
        if (content == null || content.isBlank()) {
            return Result.error("回复内容不能为空");
        }
        return reviewService.reply(reviewId, content, organizerUserId);
    }

    /**
     * 申诉评价
     */
    @PostMapping("/appeal")
    @Operation(summary = "申诉评价", description = "针对恶意差评发起申诉")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<String> appeal(@RequestParam Long reviewId) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }
        return reviewService.appeal(reviewId, organizerUserId);
    }
}

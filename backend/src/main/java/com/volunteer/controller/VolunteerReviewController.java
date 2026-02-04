package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.common.Result;
import com.volunteer.dto.ReviewRequest;
import com.volunteer.service.ReviewService;
import com.volunteer.security.SecurityUtils;
import com.volunteer.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 志愿者评价控制器
 */
@RestController
@RequestMapping("/volunteer/review")
@RequiredArgsConstructor
@Tag(name = "志愿者评价", description = "志愿者对已完成活动的评价与查看")
public class VolunteerReviewController {

    private final ReviewService reviewService;

    /**
     * 提交评价
     */
    @PostMapping("/add")
    @Operation(summary = "提交评价", description = "对已完成的活动进行评价")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<String> addReview(@Valid @RequestBody ReviewRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return reviewService.addReview(userId, request);
    }

    /**
     * 获取我的评价列表
     */
    @GetMapping("/my")
    @Operation(summary = "我的评价列表", description = "获取当前志愿者提交的所有评价")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<IPage<ReviewVO>> getMyReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return reviewService.getMyReviews(userId, pageNum, pageSize);
    }
}

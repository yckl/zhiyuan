package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.RecommendationDTO;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
@Tag(name = "推荐引擎", description = "提供基于多模态权重的活动推荐")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/home")
    @Operation(summary = "首页猜你喜欢", description = "基于用户画像和历史行为推荐4个活动")
    public Result<List<RecommendationDTO>> getHomeRecommendations() {
        Long userId = SecurityUtils.getUserId();
        List<RecommendationDTO> recommendations = recommendationService.getHomeRecommendations(userId);
        return Result.success(recommendations);
    }

    @GetMapping("/detail/{activityId}")
    @Operation(summary = "详情页关联推荐", description = "基于当前活动上下文推荐4个关联活动")
    public Result<List<RecommendationDTO>> getDetailRecommendations(@PathVariable Long activityId) {
        Long userId = SecurityUtils.getUserId();
        List<RecommendationDTO> recommendations = recommendationService.getDetailRecommendations(activityId, userId);
        return Result.success(recommendations);
    }
}

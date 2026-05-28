package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityFavorite;
import com.volunteer.mapper.ActivityFavoriteMapper;
import com.volunteer.mapper.ActivityMapper;

import com.volunteer.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 活动收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "活动收藏", description = "活动收藏的增删查")
public class ActivityFavoriteController {

    private final ActivityFavoriteMapper favoriteMapper;
    private final ActivityMapper activityMapper;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    @Operation(summary = "添加收藏", description = "收藏一个活动")
    public Result<Void> addFavorite(@RequestBody Map<String, Long> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Long activityId = params.get("activityId");
        if (activityId == null) {
            return Result.error("活动ID不能为空");
        }

        try {
            // 检查是否已收藏
            int count = favoriteMapper.checkFavorite(userId, activityId);
            if (count > 0) {
                return Result.error("已收藏该活动");
            }

            // 添加收藏
            ActivityFavorite favorite = new ActivityFavorite();
            favorite.setUserId(userId);
            favorite.setActivityId(activityId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);

            // 更新活动收藏数
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null) {
                Integer currentCount = activity.getCollectCount();
                activity.setCollectCount((currentCount != null ? currentCount : 0) + 1);
                activityMapper.updateById(activity);
            }

            return Result.success("收藏成功", null);
        } catch (Exception e) {
            log.error("添加收藏失败: {}", e.getMessage());
            return Result.error("添加收藏失败");
        }
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    @Operation(summary = "取消收藏", description = "取消收藏一个活动")
    public Result<Void> removeFavorite(@RequestBody Map<String, Long> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Long activityId = params.get("activityId");
        if (activityId == null) {
            return Result.error("活动ID不能为空");
        }

        try {
            int deleted = favoriteMapper.deleteFavorite(userId, activityId);
            if (deleted > 0) {
                // 更新活动收藏数
                Activity activity = activityMapper.selectById(activityId);
                if (activity != null && activity.getCollectCount() != null && activity.getCollectCount() > 0) {
                    activity.setCollectCount(activity.getCollectCount() - 1);
                    activityMapper.updateById(activity);
                }
            }
            return Result.success("取消收藏成功", null);
        } catch (Exception e) {
            log.error("取消收藏失败: {}", e.getMessage());
            return Result.error("取消收藏失败");
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否收藏了某活动")
    public Result<Boolean> checkFavorite(@RequestParam Long activityId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.success(false);
        }

        try {
            int count = favoriteMapper.checkFavorite(userId, activityId);
            return Result.success(count > 0);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {}", e.getMessage());
            return Result.success(false);
        }
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/list")
    @Operation(summary = "我的收藏", description = "获取当前用户收藏的活动列表")
    public Result<List<Activity>> getFavoriteList() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            List<Long> activityIds = favoriteMapper.getUserFavoriteActivityIds(userId);
            if (activityIds == null || activityIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            List<Activity> activities = activityMapper.selectBatchIds(activityIds);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取收藏列表失败: {}", e.getMessage());
            return Result.error("获取收藏列表失败");
        }
    }
}

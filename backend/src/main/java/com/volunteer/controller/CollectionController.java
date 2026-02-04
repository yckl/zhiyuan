package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.common.Result;
import com.volunteer.entity.Collection;
import com.volunteer.service.CollectionService;
import com.volunteer.vo.CollectionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 全域收藏控制器
 * 支持收藏活动、课程、公告、心得
 */
@Slf4j
@RestController
@RequestMapping("/collection")
@RequiredArgsConstructor
@Tag(name = "全域收藏", description = "统一的收藏功能，支持多种内容类型")
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 切换收藏状态
     * 如果已收藏则取消，否则添加收藏
     */
    @PostMapping("/toggle")
    @Operation(summary = "收藏/取消收藏", description = "切换收藏状态，已收藏则取消，未收藏则添加")
    public Result<Boolean> toggleFavorite(@RequestBody Map<String, Object> params) {
        Long targetId = Long.valueOf(params.get("targetId").toString());
        String targetType = params.get("targetType").toString().toUpperCase();

        // 验证类型
        if (!isValidTargetType(targetType)) {
            return Result.error("不支持的收藏类型: " + targetType);
        }

        try {
            boolean isFavorited = collectionService.toggleFavorite(targetId, targetType);
            String message = isFavorited ? "收藏成功" : "已取消收藏";
            return Result.success(message, isFavorited);
        } catch (Exception e) {
            log.error("操作收藏失败: {}", e.getMessage());
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/check")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏指定内容")
    public Result<Boolean> checkStatus(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型: ACTIVITY, COURSE, NOTICE, EXPERIENCE") @RequestParam String targetType) {

        targetType = targetType.toUpperCase();
        if (!isValidTargetType(targetType)) {
            return Result.error("不支持的收藏类型: " + targetType);
        }

        try {
            boolean isFavorited = collectionService.checkStatus(targetId, targetType);
            return Result.success(isFavorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {}", e.getMessage());
            return Result.success(false);
        }
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/list")
    @Operation(summary = "我的收藏列表", description = "分页获取当前用户的收藏列表")
    public Result<IPage<CollectionVO>> listMyFavorites(
            @Parameter(description = "目标类型: ACTIVITY, COURSE, NOTICE, EXPERIENCE") @RequestParam String targetType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {

        targetType = targetType.toUpperCase();
        if (!isValidTargetType(targetType)) {
            return Result.error("不支持的收藏类型: " + targetType);
        }

        try {
            IPage<CollectionVO> page = collectionService.listMyFavorites(targetType, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取收藏列表失败: {}", e.getMessage());
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * 验证目标类型是否有效
     */
    private boolean isValidTargetType(String targetType) {
        return Collection.TargetType.ACTIVITY.equals(targetType)
                || Collection.TargetType.COURSE.equals(targetType)
                || Collection.TargetType.NOTICE.equals(targetType)
                || Collection.TargetType.EXPERIENCE.equals(targetType);
    }
}

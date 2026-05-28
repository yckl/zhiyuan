package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.ActivityCategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共分类接口
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "公共分类接口", description = "获取活动分类列表")
public class CategoryController {

    private final ActivityCategoryMapper categoryMapper;

    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "获取所有启用的活动分类")
    public Result<List<ActivityCategory>> list() {
        return Result.success(categoryMapper.selectList(new LambdaQueryWrapper<ActivityCategory>()
                .eq(ActivityCategory::getIsDeleted, 0)
                .eq(ActivityCategory::getStatus, 1) // Only enabled
                .orderByDesc(ActivityCategory::getSortOrder)
                .orderByDesc(ActivityCategory::getCreateTime)));
    }
}

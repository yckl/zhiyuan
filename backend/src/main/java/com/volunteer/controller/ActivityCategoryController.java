package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.mapper.ActivityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "活动分类管理", description = "管理员活动分类管理")
@PreAuthorize("hasRole('ADMIN')")
public class ActivityCategoryController {

    private final ActivityCategoryMapper categoryMapper;
    private final ActivityMapper activityMapper;

    @GetMapping("/list")
    @Operation(summary = "获取分类列表")
    public Result<List<ActivityCategory>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<ActivityCategory> query = new LambdaQueryWrapper<ActivityCategory>()
                .eq(ActivityCategory::getIsDeleted, 0)
                .orderByDesc(ActivityCategory::getSortOrder)
                .orderByDesc(ActivityCategory::getCreateTime);

        if (name != null && !name.isBlank()) {
            query.like(ActivityCategory::getName, name);
        }

        return Result.success(categoryMapper.selectList(query));
    }

    @PostMapping("/add")
    @Operation(summary = "添加分类")
    public Result<String> add(@RequestBody ActivityCategory category) {
        category.setId(null); // Ensure insert
        category.setStatus(1); // Default enabled
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新分类")
    public Result<String> update(@RequestBody ActivityCategory category) {
        categoryMapper.updateById(category);
        return Result.success("更新成功");
    }

    @PutMapping("/status")
    @Operation(summary = "切换分类状态")
    public Result<String> toggleStatus(@RequestBody ActivityCategory category) {
        // Only ID and Status needed
        if (category.getId() == null || category.getStatus() == null) {
            return Result.error("参数错误");
        }
        categoryMapper.updateById(category);
        return Result.success("操作成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分类")
    public Result<String> delete(@PathVariable Long id) {
        // Check usage
        Long count = activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCategoryId, id)
                .eq(Activity::getIsDeleted, 0));

        if (count > 0) {
            return Result.error("该分类下仍有 " + count + " 个活动，无法删除");
        }

        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

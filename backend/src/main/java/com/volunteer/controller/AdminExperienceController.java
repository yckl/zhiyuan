package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.Experience;
import com.volunteer.mapper.ExperienceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/experience")
@RequiredArgsConstructor
@Tag(name = "管理员心得管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExperienceController {

    private final ExperienceMapper experienceMapper;

    @GetMapping("/list")
    @Operation(summary = "获取心得列表")
    public Result<IPage<Experience>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Experience> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Experience> query = new LambdaQueryWrapper<Experience>()
                .eq(Experience::getIsDeleted, 0)
                .orderByDesc(Experience::getCreateTime);

        if (status != null) {
            query.eq(Experience::getStatus, status); // 0-Pending, 1-Published
        }

        return Result.success(experienceMapper.selectPage(pageParam, query));
    }

    @PutMapping("/audit")
    @Operation(summary = "审核心得")
    public Result<String> audit(@RequestBody Experience experience) {
        // Param: id, status (1-Pass)
        if (experience.getId() == null || experience.getStatus() == null) {
            return Result.error("参数错误");
        }
        experienceMapper.updateById(experience);
        return Result.success("审核完成");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除/违规处理")
    public Result<String> delete(@PathVariable Long id) {
        experienceMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

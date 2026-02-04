package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.Banner;
import com.volunteer.mapper.BannerMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
@Tag(name = "管理员轮播图管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerMapper bannerMapper;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getIsDeleted, 0)
                .orderByDesc(Banner::getSortOrder)
                .orderByDesc(Banner::getCreateTime)));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Banner banner) {
        banner.setId(null);
        banner.setStatus(1);
        bannerMapper.insert(banner);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Banner banner) {
        bannerMapper.updateById(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.Banner;
import com.volunteer.mapper.BannerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开轮播图接口
 * 供前端首页使用
 */
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
@Tag(name = "轮播图", description = "首页轮播图公开接口")
public class BannerController {

    private final BannerMapper bannerMapper;

    @GetMapping("/list")
    @Operation(summary = "获取轮播图列表", description = "获取已启用的轮播图，按排序号降序")
    public Result<List<Banner>> list() {
        List<Banner> banners = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1) // 只返回启用的
                        .eq(Banner::getIsDeleted, 0) // 未删除
                        .orderByDesc(Banner::getSortOrder) // 排序号降序
                        .orderByDesc(Banner::getCreateTime) // 创建时间降序
        );
        return Result.success(banners);
    }
}

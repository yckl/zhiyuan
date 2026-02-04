package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.Notice;
import com.volunteer.mapper.NoticeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
@Tag(name = "管理员公告管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    private final NoticeMapper noticeMapper;

    @GetMapping("/list")
    @Operation(summary = "获取公告列表")
    public Result<IPage<Notice>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> query = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getIsDeleted, 0)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime);

        if (title != null && !title.isBlank()) {
            query.like(Notice::getTitle, title);
        }

        return Result.success(noticeMapper.selectPage(pageParam, query));
    }

    @PostMapping("/add")
    @Operation(summary = "发布公告")
    public Result<String> add(@RequestBody Notice notice) {
        notice.setId(null);
        notice.setStatus(1); // Published
        notice.setPublishTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return Result.success("发布成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新公告")
    public Result<String> update(@RequestBody Notice notice) {
        noticeMapper.updateById(notice);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除公告")
    public Result<String> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

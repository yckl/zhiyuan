package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.common.Result;
import com.volunteer.entity.Notice;
import com.volunteer.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Tag(name = "公告通知", description = "公告与通知管理")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 公告列表
     */
    @GetMapping("/list")
    @Operation(summary = "公告列表", description = "分页获取公告列表")
    public Result<IPage<Notice>> listNotices(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            IPage<Notice> page = noticeService.pageNotices(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取公告列表失败: {}", e.getMessage());
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "公告详情", description = "获取公告详情")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getNoticeDetail(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            noticeService.incrementViewCount(id);
            return Result.success(notice);
        } catch (Exception e) {
            log.error("获取公告详情失败: {}", e.getMessage());
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }
}

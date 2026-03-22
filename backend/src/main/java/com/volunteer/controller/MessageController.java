package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.SysMessage;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "站内信", description = "用户消息中心接口")
public class MessageController {

    private final SysMessageService sysMessageService;

    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "分页获取当前用户的消息")
    public Result<IPage<SysMessage>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Page<SysMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysMessage> queryWrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .orderByDesc(SysMessage::getCreateTime);

        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            if (type.contains(",")) {
                queryWrapper.in(SysMessage::getType, (Object[]) type.split(","));
            } else {
                queryWrapper.eq(SysMessage::getType, type);
            }
        }

        return Result.success(sysMessageService.page(page, queryWrapper));
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "获取未读数量", description = "获取当前用户未读消息数量")
    public Result<Long> getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
            return Result.success(0L);

        long count = sysMessageService.count(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getIsRead, 0));
        return Result.success(count);
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记单条已读")
    public Result<Void> readMessage(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        SysMessage message = sysMessageService.getById(id);
        if (message != null && message.getReceiverId().equals(userId)) {
            message.setIsRead(1);
            sysMessageService.updateById(message);
        }
        return Result.success();
    }

    @PostMapping("/readAll")
    @Operation(summary = "全部标记已读")
    public Result<Void> readAll() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        // update sys_message set is_read = 1 where receiver_id = ? and is_read = 0
        sysMessageService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getIsRead, 0)
                .set(SysMessage::getIsRead, 1));
        return Result.success();
    }
}

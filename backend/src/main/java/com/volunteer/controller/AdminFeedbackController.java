package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.Feedback;
import com.volunteer.mapper.FeedbackMapper;
import com.volunteer.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
@Tag(name = "管理员反馈管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final SysMessageService sysMessageService; // Send notification

    @GetMapping("/list")
    public Result<IPage<Feedback>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Feedback> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Feedback> query = new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getIsDeleted, 0)
                .orderByDesc(Feedback::getCreateTime);

        if (status != null) {
            query.eq(Feedback::getStatus, status);
        }

        return Result.success(feedbackMapper.selectPage(pageParam, query));
    }

    @PutMapping("/reply")
    @Operation(summary = "处理反馈")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reply(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String replyContent = (String) body.get("replyContent");

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null)
            return Result.error("反馈不存在");

        feedback.setReplyContent(replyContent);
        feedback.setStatus(1); // Handled
        feedbackMapper.updateById(feedback);

        // Send System Message if user not anonymous
        if (feedback.getUserId() != null) {
            sysMessageService.sendMessage(
                    feedback.getUserId(),
                    1L, // System, assume admin id 1
                    "【系统通知】您的反馈已处理",
                    "您提交的反馈收到回复：" + replyContent,
                    "SYSTEM");
        }

        return Result.success("处理成功");
    }
}

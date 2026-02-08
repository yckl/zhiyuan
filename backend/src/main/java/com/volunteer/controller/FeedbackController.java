package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Feedback;
import com.volunteer.mapper.FeedbackMapper;
import com.volunteer.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户反馈提交接口
 * 供志愿者/组织者提交反馈
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Tag(name = "用户反馈", description = "问题反馈与建议提交")
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;

    @PostMapping("/submit")
    @Operation(summary = "提交反馈", description = "用户提交问题反馈或建议")
    public Result<String> submit(@RequestBody Feedback feedback) {
        // 验证必填字段
        if (!StringUtils.hasText(feedback.getContent())) {
            return Result.error("请填写反馈内容");
        }
        if (!StringUtils.hasText(feedback.getContactInfo())) {
            return Result.error("请填写联系方式");
        }

        // 设置用户信息（如果已登录）
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            feedback.setUserId(userId);
        }

        // 初始化状态
        feedback.setId(null);
        feedback.setStatus(0); // 待处理
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        feedback.setIsDeleted(0);

        feedbackMapper.insert(feedback);

        return Result.success("反馈提交成功，我们会尽快处理！");
    }
}

package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.dto.RegistrationRequest;
import com.volunteer.vo.RegistrationStatsVO;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动报名控制器
 */
@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
@Tag(name = "活动报名", description = "活动报名管理")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final ActivityMapper activityMapper;

    /**
     * 报名活动
     */
    @PostMapping
    @Operation(summary = "报名活动", description = "志愿者报名参加活动，可选择使用免审核卡")
    public Result<ActivityRegistration> register(
            @Valid @RequestBody RegistrationRequest request,
            @RequestParam(required = false, defaultValue = "false") Boolean useSkipCard) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            ActivityRegistration registration = registrationService.register(request, userId, useSkipCard);
            String message = Boolean.TRUE.equals(useSkipCard) ? "报名成功（已使用免审核卡）" : "报名成功";
            return Result.success(message, registration);
        } catch (Exception e) {
            log.error("报名失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消报名
     */
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消报名", description = "志愿者取消报名")
    public Result<Void> cancelRegistration(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            registrationService.cancelRegistration(id, userId, reason);
            return Result.success("取消成功", null);
        } catch (Exception e) {
            log.error("取消报名失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 我的报名记录
     */
    @GetMapping("/my")
    @Operation(summary = "我的报名", description = "获取当前用户的报名记录")
    public Result<Page<RegistrationDTO>> getMyRegistrations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Page<RegistrationDTO> result = registrationService.getMyRegistrations(userId, page, size, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询我的报名失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动的报名列表（组织者）
     */
    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "活动报名列表", description = "组织者查看活动的报名人员")
    public Result<Page<RegistrationDTO>> getActivityRegistrations(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            // 水平越权检查：非管理员只能查看自己创建的活动的报名列表
            Long userId = SecurityUtils.getUserId();
            if (!SecurityUtils.isAdmin()) {
                Activity activity = activityMapper.selectById(activityId);
                if (activity == null) {
                    return Result.error("活动不存在");
                }
                if (!activity.getOrganizerId().equals(userId)) {
                    return Result.error("无权查看此活动的报名列表");
                }
            }

            Page<RegistrationDTO> result = registrationService.getActivityRegistrations(activityId, page, size, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询活动报名列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取指定志愿者的报名历史 (组织者审核时查看)
     */
    @GetMapping("/volunteer/{volunteerId}/history")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "志愿者报名历史", description = "组织者审核时查看该志愿者的过往活动记录")
    public Result<Page<RegistrationDTO>> getVolunteerHistory(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 这里 status 传 3 表示只看已完成的，或者不传看全部。通常审核时看全部状态更有参考价值。
            Page<RegistrationDTO> result = registrationService.getVolunteerRegistrations(volunteerId, page, size, null);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询志愿者报名历史失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核报名 (单个)
     */
    @PostMapping("/audit")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "审核报名", description = "组织者审核报名申请")
    public Result<Void> auditRegistration(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Long id = Long.valueOf(params.get("id").toString());
            boolean pass = Boolean.TRUE.equals(params.get("pass"));
            String reason = params.get("reason") != null ? params.get("reason").toString() : null;

            int status = pass ? ActivityRegistration.STATUS_CONFIRMED : ActivityRegistration.STATUS_REJECTED;
            registrationService.auditRegistration(id, status, reason, userId);
            return Result.success("审核完成", null);
        } catch (Exception e) {
            log.error("审核报名失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量审核报名
     */
    @PostMapping("/audit/batch")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "批量审核报名", description = "组织者批量审核报名申请")
    public Result<Void> batchAuditRegistration(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) params.get("ids");
            boolean pass = Boolean.TRUE.equals(params.get("pass"));
            String reason = params.get("reason") != null ? params.get("reason").toString() : null;

            int status = pass ? 1 : 2;
            int count = 0;
            for (Number id : ids) {
                try {
                    registrationService.auditRegistration(id.longValue(), status, reason, userId);
                    count++;
                } catch (Exception e) {
                    log.warn("审核ID {} 失败: {}", id, e.getMessage());
                }
            }
            return Result.success("批量审核完成，成功处理 " + count + " 条", null);
        } catch (Exception e) {
            log.error("批量审核失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 签到
     */
    @PostMapping("/signin/{id}")
    @Operation(summary = "签到", description = "志愿者签到")
    public Result<Void> signIn(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            registrationService.signIn(id, userId);
            return Result.success("签到成功", null);
        } catch (Exception e) {
            log.error("签到失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 输码签到
     */
    @PostMapping("/signin/code")
    @Operation(summary = "输码签到", description = "志愿者通过输入签到码签到")
    public Result<Void> signInByCode(@RequestBody Map<String, String> params) {
        String code = params.get("code");

        if (code == null || code.length() != 6) {
            return Result.error("请输入6位签到码");
        }

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            registrationService.signInByCode(code, userId);
            return Result.success("签到成功", null);
        } catch (Exception e) {
            log.error("输码签到失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rate/{id}")
    @Operation(summary = "评价志愿者", description = "活动结束后，主办方对志愿者进行评价")
    public Result<Void> rateVolunteer(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer rating = (Integer) params.get("rating");
        String comment = (String) params.get("comment");

        if (rating == null || rating < 1 || rating > 5) {
            return Result.error("评分必须在1-5之间");
        }

        // 获取当前操作者ID
        Long operatorId = SecurityUtils.getUserId();

        try {
            registrationService.rateVolunteer(id, rating,
                    comment, operatorId);
            return Result.success("评价成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 签退
     */
    @PostMapping("/signout/{id}")
    @Operation(summary = "签退", description = "志愿者签退")
    public Result<Void> signOut(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            registrationService.signOut(id, userId);
            return Result.success("签退成功", null);
        } catch (Exception e) {
            log.error("签退失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已报名
     */
    @GetMapping("/check/{activityId}")
    @Operation(summary = "检查报名状态", description = "检查当前用户是否已报名该活动")
    public Result<Boolean> checkRegistration(@PathVariable Long activityId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.success(false);
        }

        try {
            // 需要先获取志愿者ID
            boolean registered = registrationService.hasRegisteredByUserId(activityId, userId);
            return Result.success(registered);
        } catch (Exception e) {
            log.error("检查报名状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 单个结算 (前端兼容接口)
     */
    @PostMapping("/settle")
    @Operation(summary = "单个结算", description = "结算单个志愿者的工时和积分")
    public Result<Void> settleSingle(@RequestBody Map<String, Object> params) {
        // 委托给 SettlementController，这里简单返回成功
        // 实际逻辑在 SettlementController 中
        return Result.error(400, "请使用 /organizer/settlement/settle 接口");
    }

    /**
     * 批量结算 (前端兼容接口)
     */
    @PostMapping("/settle/batch")
    @Operation(summary = "批量结算", description = "批量结算志愿者工时和积分")
    public Result<Void> settleBatch(@RequestBody Map<String, Object> params) {
        // 委托给 SettlementController，这里简单返回成功
        // 实际逻辑在 SettlementController 中
        return Result.error(400, "请使用 /organizer/settlement/batch 接口");
    }

    /**
     * 获取活动报名统计数据
     */
    @GetMapping("/stats/{activityId}")
    @Operation(summary = "活动报名统计", description = "获取活动的报名人数统计（总人数、已通过、待审核、已拒绝）")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<RegistrationStatsVO> getRegistrationStats(@PathVariable Long activityId) {
        try {
            RegistrationStatsVO stats = registrationService.getRegistrationStats(activityId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("查询报名统计失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

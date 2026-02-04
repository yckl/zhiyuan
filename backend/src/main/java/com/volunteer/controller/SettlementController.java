package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.entity.SysMessage;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityRegistrationMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.SysMessageService;
import com.volunteer.service.VolunteerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 工时结算控制器
 */
@Slf4j
@RestController
@RequestMapping("/organizer/settlement")
@RequiredArgsConstructor
@Tag(name = "工时结算", description = "组织者工时结算与积分发放")
@PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
public class SettlementController {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final VolunteerMapper volunteerMapper;
    private final VolunteerService volunteerService;
    private final SysMessageService messageService;

    /**
     * 获取活动的已签到人员列表
     */
    @GetMapping("/activity/{activityId}/attendees")
    @Operation(summary = "获取签到人员", description = "获取活动所有已签到志愿者列表")
    public Result<Map<String, Object>> getAttendees(@PathVariable Long activityId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!activity.getOrganizerId().equals(userId)) {
            return Result.error("无权操作此活动");
        }

        // 查询已签到的报名记录
        LambdaQueryWrapper<ActivityRegistration> query = Wrappers.<ActivityRegistration>lambdaQuery()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getIsDeleted, 0)
                .isNotNull(ActivityRegistration::getSignInTime)
                .orderByAsc(ActivityRegistration::getSignInTime);

        List<ActivityRegistration> registrations = registrationMapper.selectList(query);

        // 构建返回数据
        List<Map<String, Object>> attendeeList = new ArrayList<>();
        for (ActivityRegistration reg : registrations) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("registrationId", reg.getId());
            item.put("volunteerId", reg.getVolunteerId());

            // 获取志愿者信息
            Volunteer volunteer = volunteerMapper.selectById(reg.getVolunteerId());
            if (volunteer != null) {
                item.put("volunteerName", volunteer.getName());
                item.put("studentId", volunteer.getStudentNo());
                item.put("college", volunteer.getCollege());
            }

            item.put("signInTime", reg.getSignInTime());
            item.put("signOutTime", reg.getSignOutTime());
            item.put("actualHours", reg.getActualHours());
            item.put("actualPoints", reg.getActualPoints());
            item.put("status", reg.getStatus());
            item.put("settled", reg.getStatus() != null && reg.getStatus() >= ActivityRegistration.STATUS_COMPLETED);

            attendeeList.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activity", Map.of(
                "id", activity.getId(),
                "title", activity.getTitle(),
                "serviceHours", activity.getServiceHours(),
                "pointsReward", activity.getPointsReward()));
        result.put("attendees", attendeeList);
        result.put("totalCount", attendeeList.size());
        result.put("settledCount", attendeeList.stream().filter(a -> (Boolean) a.get("settled")).count());

        return Result.success(result);
    }

    /**
     * 单个结算
     */
    @PostMapping("/settle")
    @Operation(summary = "单个结算", description = "结算单个志愿者的工时和积分")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> settleSingle(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Long registrationId = Long.valueOf(params.get("registrationId").toString());
            BigDecimal hours = new BigDecimal(params.get("hours").toString());
            Integer rating = params.get("rating") != null ? Integer.valueOf(params.get("rating").toString()) : 5;

            return doSettle(registrationId, hours, rating, userId);
        } catch (Exception e) {
            log.error("结算失败: {}", e.getMessage());
            return Result.error("结算失败: " + e.getMessage());
        }
    }

    /**
     * 批量结算
     */
    @PostMapping("/batch")
    @Operation(summary = "批量结算", description = "批量结算志愿者工时和积分")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> settleBatch(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");

            int success = 0;
            int failed = 0;
            List<String> errors = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Long registrationId = Long.valueOf(item.get("registrationId").toString());
                BigDecimal hours = new BigDecimal(item.get("hours").toString());
                Integer rating = item.get("rating") != null ? Integer.valueOf(item.get("rating").toString()) : 5;

                Result<Void> result = doSettle(registrationId, hours, rating, userId);
                if (result.getCode() == 200) {
                    success++;
                } else {
                    failed++;
                    errors.add("ID " + registrationId + ": " + result.getMessage());
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", success);
            result.put("failed", failed);
            result.put("errors", errors);

            return Result.success("批量结算完成", result);
        } catch (Exception e) {
            log.error("批量结算失败: {}", e.getMessage());
            return Result.error("批量结算失败: " + e.getMessage());
        }
    }

    /**
     * 执行结算逻辑
     */
    private Result<Void> doSettle(Long registrationId, BigDecimal hours, Integer rating, Long operatorId) {
        // 1. 获取报名记录
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            return Result.error("报名记录不存在");
        }

        // 2. 检查是否已结算
        if (registration.getStatus() != null && registration.getStatus() >= ActivityRegistration.STATUS_COMPLETED) {
            return Result.error("该记录已结算");
        }

        // 3. 验证活动归属
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || !activity.getOrganizerId().equals(operatorId)) {
            return Result.error("无权操作此活动");
        }

        // 4. 计算积分 (工时 * 10)
        int points = hours.multiply(BigDecimal.TEN).intValue();

        // 5. 更新报名记录
        registration.setActualHours(hours);
        registration.setActualPoints(points);
        registration.setStatus(ActivityRegistration.STATUS_COMPLETED);
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 6. 增加志愿者积分和工时
        try {
            volunteerService.addPoints(registration.getVolunteerId(), points,
                    "活动【" + activity.getTitle() + "】工时结算");
            volunteerService.addServiceHours(registration.getVolunteerId(), hours);
        } catch (Exception e) {
            log.warn("积分/工时更新失败: {}", e.getMessage());
        }

        // 7. 发送通知
        try {
            messageService.sendMessage(
                    registration.getVolunteerId(),
                    operatorId,
                    "工时结算通知",
                    String.format("您在活动【%s】的服务已完成结算。\n服务时长: %.1f 小时\n获得积分: %d 分\n感谢您的付出！",
                            activity.getTitle(), hours.doubleValue(), points),
                    SysMessage.TYPE_NOTICE);
        } catch (Exception e) {
            log.warn("发送结算通知失败: {}", e.getMessage());
        }

        log.info("结算成功: registrationId={}, hours={}, points={}", registrationId, hours, points);
        return Result.success("结算成功", null);
    }

    /**
     * 获取结算统计
     */
    @GetMapping("/stats")
    @Operation(summary = "结算统计", description = "获取组织者的结算统计数据")
    public Result<Map<String, Object>> getStats() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取组织者的所有活动
        List<Activity> activities = activityMapper.selectList(
                Wrappers.<Activity>lambdaQuery().eq(Activity::getOrganizerId, userId));

        if (activities.isEmpty()) {
            return Result.success(Map.of(
                    "totalActivities", 0,
                    "totalSettled", 0,
                    "totalPending", 0,
                    "totalHours", 0.0,
                    "totalPoints", 0));
        }

        List<Long> activityIds = activities.stream().map(Activity::getId).toList();

        // 统计已结算
        long settled = registrationMapper.selectCount(
                Wrappers.<ActivityRegistration>lambdaQuery()
                        .in(ActivityRegistration::getActivityId, activityIds)
                        .eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_COMPLETED));

        // 统计待结算 (已签到未完成)
        long pending = registrationMapper.selectCount(
                Wrappers.<ActivityRegistration>lambdaQuery()
                        .in(ActivityRegistration::getActivityId, activityIds)
                        .isNotNull(ActivityRegistration::getSignInTime)
                        .lt(ActivityRegistration::getStatus, ActivityRegistration.STATUS_COMPLETED));

        // 统计总工时和积分
        List<ActivityRegistration> completedRegs = registrationMapper.selectList(
                Wrappers.<ActivityRegistration>lambdaQuery()
                        .in(ActivityRegistration::getActivityId, activityIds)
                        .eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_COMPLETED));

        double totalHours = completedRegs.stream()
                .filter(r -> r.getActualHours() != null)
                .mapToDouble(r -> r.getActualHours().doubleValue())
                .sum();

        int totalPoints = completedRegs.stream()
                .filter(r -> r.getActualPoints() != null)
                .mapToInt(ActivityRegistration::getActualPoints)
                .sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalActivities", activities.size());
        stats.put("totalSettled", settled);
        stats.put("totalPending", pending);
        stats.put("totalHours", Math.round(totalHours * 10.0) / 10.0);
        stats.put("totalPoints", totalPoints);

        return Result.success(stats);
    }
}

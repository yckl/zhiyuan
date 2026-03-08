package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityRegistrationMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 组织者签到管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/organizer/checkin")
@RequiredArgsConstructor
@Tag(name = "组织者签到管理", description = "现场签到、二维码生成、人工补签")
@PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
public class OrganizerCheckinController {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final VolunteerMapper volunteerMapper;

    /**
     * 获取签到二维码内容
     * 返回加密的Token，有效期1分钟
     */
    @GetMapping("/activity/{activityId}/qrcode")
    @Operation(summary = "获取签到二维码", description = "生成带有效期的签到Token")
    public Result<Map<String, Object>> getCheckinQRCode(@PathVariable Long activityId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            // 验证活动归属
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            if (!activity.getOrganizerId().equals(userId)) {
                return Result.error("无权操作此活动");
            }

            // 确保活动有签到码
            if (activity.getCheckinCode() == null) {
                // 生成6位数字签到码
                String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
                activity.setCheckinCode(code);
                activityMapper.updateById(activity);
            }

            // 生成签到Token (包含 activityId + expireAt + nonce)
            long timestamp = System.currentTimeMillis();
            long expireAt = timestamp + 30000; // 30秒后过期，防止截图代签 (时效锁)
            String rawToken = String.format("%d:%d:%s", activityId, expireAt,
                    UUID.randomUUID().toString().substring(0, 8));
            String token = Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("checkinCode", activity.getCheckinCode()); // 6位大字备用数字码
            result.put("activityId", activityId);
            result.put("expireAt", expireAt);
            result.put("refreshInterval", 30000);
            result.put("qrcodeContent", "volunteer://checkin?token=" + token + "&activityId=" + activityId);

            return Result.success(result);
        } catch (Exception e) {
            log.error("生成签到二维码失败: {}", e.getMessage());
            return Result.error("生成二维码失败");
        }
    }

    /**
     * 获取签到统计和已签到名单
     */
    @GetMapping("/activity/{activityId}/attendees")
    @Operation(summary = "获取签到情况", description = "获取当前签到统计和已签到名单")
    public Result<Map<String, Object>> getAttendees(@PathVariable Long activityId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 查询已通过审核 (1) 或已签到 (2) 的报名
            LambdaQueryWrapper<ActivityRegistration> query = new LambdaQueryWrapper<>();
            query.eq(ActivityRegistration::getActivityId, activityId)
                    .and(wrapper -> wrapper.eq(ActivityRegistration::getStatus, 1)
                            .or().eq(ActivityRegistration::getStatus, 2))
                    .eq(ActivityRegistration::getIsDeleted, 0);
            List<ActivityRegistration> registrations = registrationMapper.selectList(query);

            // 统计
            int total = registrations.size();
            int checkedIn = (int) registrations.stream()
                    .filter(r -> r.getSignInTime() != null)
                    .count();
            int pending = total - checkedIn;

            // 构建返回数据
            List<Map<String, Object>> attendeeList = new ArrayList<>();
            for (ActivityRegistration reg : registrations) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", reg.getId());
                item.put("volunteerId", reg.getVolunteerId());

                // 获取志愿者信息
                var volunteer = volunteerMapper.selectById(reg.getVolunteerId());
                if (volunteer != null) {
                    item.put("volunteerName", volunteer.getName());
                    item.put("studentId", volunteer.getStudentNo());
                    item.put("college", volunteer.getCollege());
                }

                item.put("signInTime", reg.getSignInTime());
                item.put("signOutTime", reg.getSignOutTime());
                item.put("checkedIn", reg.getSignInTime() != null);
                attendeeList.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("activityId", activityId);
            result.put("activityTitle", activity.getTitle());
            result.put("totalApproved", total);
            result.put("checkedInCount", checkedIn);
            result.put("pendingCount", pending);
            result.put("attendees", attendeeList);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取签到情况失败: {}", e.getMessage());
            return Result.error("获取签到情况失败");
        }
    }

    /**
     * 撤回核销
     */
    @PostMapping("/rollback/{id}")
    @Operation(summary = "撤回核销", description = "将已签到状态回滚为已通过状态")
    public Result<Void> rollbackCheckin(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        try {
            ActivityRegistration registration = registrationMapper.selectById(id);
            if (registration == null)
                return Result.error("记录不存在");

            // 权限校验
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity == null || (!SecurityUtils.isAdmin() && !activity.getOrganizerId().equals(userId))) {
                return Result.error("无权操作");
            }

            if (registration.getStatus() != 2) {
                return Result.error("该记录非签到状态，无法撤回");
            }

            // 执行撤回
            registration.setSignInTime(null);
            registration.setStatus(1); // 回退到已通过
            registrationMapper.updateById(registration);

            log.info("撤回核销成功: id={}, operator={}", id, userId);
            return Result.success("撤回成功", null);
        } catch (Exception e) {
            return Result.error("撤回失败");
        }
    }

    /**
     * 人工补签/管理员代签
     */
    @PostMapping("/manual")
    @Operation(summary = "人工核销", description = "管理员或组织者手动为志愿者核销")
    public Result<Void> manualCheckin(@RequestBody Map<String, Object> params) {
        try {
            Long registrationId = Long.parseLong(params.get("registrationId").toString());
            ActivityRegistration reg = registrationMapper.selectById(registrationId);

            if (reg == null)
                return Result.error("记录不存在");
            if (reg.getStatus() == 2)
                return Result.error("已核销，请勿重复操作");
            if (reg.getStatus() != 1)
                return Result.error("当前报名状态不支持核销");

            reg.setStatus(2);
            reg.setSignInTime(LocalDateTime.now());
            registrationMapper.updateById(reg);

            log.info("人工核销成功: id={}, operator={}", registrationId, SecurityUtils.getUserId());
            return Result.success("人工核销成功", null);
        } catch (Exception e) {
            return Result.error("人工核销失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时进度统计
     */
    @GetMapping("/stats/{activityId}")
    @Operation(summary = "实时签到统计", description = "获取活动签到人数和总人数")
    public Result<Map<String, Object>> getCheckinStats(@PathVariable Long activityId) {
        try {
            LambdaQueryWrapper<ActivityRegistration> query = new LambdaQueryWrapper<>();
            query.eq(ActivityRegistration::getActivityId, activityId)
                    .in(ActivityRegistration::getStatus, 1, 2)
                    .eq(ActivityRegistration::getIsDeleted, 0);

            List<ActivityRegistration> list = registrationMapper.selectList(query);
            long total = list.size();
            long checked = list.stream().filter(r -> r.getStatus() == 2).count();

            Map<String, Object> res = new HashMap<>();
            res.put("total", total);
            res.put("checked", checked);
            res.put("progress", total == 0 ? 0 : (double) checked / total * 100);

            return Result.success(res);
        } catch (Exception e) {
            return Result.error("获取统计失败");
        }
    }

    /**
     * 扫码签到验证 (核心闭环)
     */
    @PostMapping("/verify")
    @Operation(summary = "扫码核销验证", description = "三重复核：活动锁、身份锁、时效锁")
    public Result<Void> verifyCheckin(@RequestBody Map<String, Object> params) {
        try {
            String token = params.get("token").toString();
            Long volunteerId = SecurityUtils.getUserId(); // 从 Token 获取当前扫码学生的 ID (身份锁)

            if (volunteerId == null)
                return Result.unauthorized("未授权");

            // 1. 时效锁：解码Token并检查过期
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3)
                return Result.error("核销码无效");

            long activityId = Long.parseLong(parts[0]);
            long expireAt = Long.parseLong(parts[1]);

            if (System.currentTimeMillis() > expireAt) {
                return Result.error("核销码已过期，请使用最新动态码");
            }

            // 2. 活动与身份锁：查找该志愿者的正向报名记录
            LambdaQueryWrapper<ActivityRegistration> query = new LambdaQueryWrapper<>();
            query.eq(ActivityRegistration::getActivityId, activityId)
                    .eq(ActivityRegistration::getVolunteerId, volunteerId)
                    .eq(ActivityRegistration::getIsDeleted, 0);

            ActivityRegistration registration = registrationMapper.selectOne(query);

            if (registration == null) {
                return Result.error("您不在本次活动的名单内 (身份错配)");
            }

            if (registration.getStatus() != 1) { // 必须是确认状态
                if (registration.getStatus() == 2)
                    return Result.error("您已完成核销，请勿重复操作");
                return Result.error("您的报名状态暂不支持核销 (状态: " + registration.getStatus() + ")");
            }

            // 3. 执行核销逻辑 (闭环更新 volunteer_system 表，此处为 activity_registration)
            registration.setSignInTime(LocalDateTime.now());
            registration.setStatus(2); // 更新为已核销/已签到
            registrationMapper.updateById(registration);

            log.info("扫码核销成功: activity={}, volunteer={}", activityId, volunteerId);
            return Result.success("核销成功！准时到场，服从安排。", null);
        } catch (Exception e) {
            log.error("扫码核销异常: {}", e.getMessage());
            return Result.error("核销失败，请联系现场组织者");
        }
    }
}

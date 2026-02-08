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

            // 生成签到Token (简单实现：activityId + timestamp + 随机数的Base64编码)
            long timestamp = System.currentTimeMillis();
            long expireAt = timestamp + 60000; // 1分钟后过期
            String rawToken = String.format("%d:%d:%s", activityId, expireAt,
                    UUID.randomUUID().toString().substring(0, 8));
            String token = Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("checkinCode", activity.getCheckinCode()); // 返回签到码
            result.put("activityId", activityId);
            result.put("expireAt", expireAt);
            result.put("qrcodeContent", "volunteer://checkin?token=" + token);

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

            // 查询已通过审核的报名 (status = 1)
            LambdaQueryWrapper<ActivityRegistration> query = new LambdaQueryWrapper<>();
            query.eq(ActivityRegistration::getActivityId, activityId)
                    .eq(ActivityRegistration::getStatus, 1)
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
     * 人工补签
     */
    @PostMapping("/manual")
    @Operation(summary = "人工补签", description = "组织者手动为志愿者签到")
    public Result<Void> manualCheckin(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Long registrationId = Long.valueOf(params.get("registrationId").toString());

            // 获取报名记录
            ActivityRegistration registration = registrationMapper.selectById(registrationId);
            if (registration == null) {
                return Result.error("报名记录不存在");
            }

            // 验证活动归属
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity == null || !activity.getOrganizerId().equals(userId)) {
                return Result.error("无权操作此活动");
            }

            // 检查是否已签到
            if (registration.getSignInTime() != null) {
                return Result.error("该志愿者已签到");
            }

            // 执行签到
            registration.setSignInTime(LocalDateTime.now());
            registration.setStatus(2); // 已签到
            registrationMapper.updateById(registration);

            log.info("人工补签成功: registrationId={}, operator={}", registrationId, userId);
            return Result.success("签到成功", null);
        } catch (Exception e) {
            log.error("人工补签失败: {}", e.getMessage());
            return Result.error("签到失败：" + e.getMessage());
        }
    }

    /**
     * 扫码签到验证
     */
    @PostMapping("/verify")
    @Operation(summary = "扫码签到验证", description = "验证签到Token并执行签到")
    public Result<Void> verifyCheckin(@RequestBody Map<String, Object> params) {
        try {
            String token = params.get("token").toString();
            Long volunteerId = Long.valueOf(params.get("volunteerId").toString());

            // 解码Token
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return Result.error("无效的签到码");
            }

            long activityId = Long.parseLong(parts[0]);
            long expireAt = Long.parseLong(parts[1]);

            // 检查是否过期
            if (System.currentTimeMillis() > expireAt) {
                return Result.error("签到码已过期，请刷新二维码");
            }

            // 查找该志愿者的报名记录
            LambdaQueryWrapper<ActivityRegistration> query = new LambdaQueryWrapper<>();
            query.eq(ActivityRegistration::getActivityId, activityId)
                    .eq(ActivityRegistration::getVolunteerId, volunteerId)
                    .eq(ActivityRegistration::getStatus, 1) // 已通过审核
                    .eq(ActivityRegistration::getIsDeleted, 0);
            ActivityRegistration registration = registrationMapper.selectOne(query);

            if (registration == null) {
                return Result.error("未找到有效的报名记录");
            }

            if (registration.getSignInTime() != null) {
                return Result.error("您已签到，无需重复签到");
            }

            // 执行签到
            registration.setSignInTime(LocalDateTime.now());
            registration.setStatus(2);
            registrationMapper.updateById(registration);

            return Result.success("签到成功", null);
        } catch (Exception e) {
            log.error("扫码签到验证失败: {}", e.getMessage());
            return Result.error("签到失败：" + e.getMessage());
        }
    }
}

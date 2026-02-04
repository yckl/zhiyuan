package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.entity.SysMessage;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityRegistrationMapper;
import com.volunteer.service.NotificationService;
import com.volunteer.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final SysMessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> broadcast(Long activityId, String content, Long senderId) {
        // 使用增强版方法，默认发给已通过者
        return broadcastWithGroup(activityId, "approved", null, content, senderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> broadcastWithGroup(Long activityId, String targetGroup, String title, String content,
            Long senderId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 校验权限：只能广播自己发布的活动
        if (!activity.getOrganizerId().equals(senderId)) {
            return Result.error("您无权为此活动发送通知");
        }

        // 根据目标群体查询志愿者ID列表
        List<Long> volunteerIds = getTargetVolunteerIds(activityId, targetGroup);

        if (volunteerIds.isEmpty()) {
            return Result.success("该活动暂无符合条件的志愿者");
        }

        // 构建标题: 格式 【目标群体 | 活动名称】标题
        // 这样前端 NotificationHistory 可以解析出 Activity 和 Target
        String targetLabel = getTargetGroupLabel(targetGroup);
        String msgTitle = title;

        if (msgTitle == null || msgTitle.isBlank()) {
            msgTitle = "通知";
        }

        // 统一格式前缀
        String prefix = String.format("【%s | %s】", targetLabel, activity.getTitle());
        // 如果标题本身不包含该前缀，则添加 (避免重复添加)
        if (!msgTitle.startsWith("【")) {
            msgTitle = prefix + msgTitle;
        }

        // 批量发送消息
        messageService.sendBatchMessages(
                volunteerIds,
                senderId,
                msgTitle,
                content,
                SysMessage.TYPE_ACTIVITY);

        log.info("Broadcast notification: activityId={}, targetGroup={}, count={}, senderId={}",
                activityId, targetGroup, volunteerIds.size(), senderId);

        return Result.success("成功发送给 " + volunteerIds.size() + " 人");
    }

    /**
     * 根据目标群体获取志愿者ID列表
     */
    private List<Long> getTargetVolunteerIds(Long activityId, String targetGroup) {
        LambdaQueryWrapper<ActivityRegistration> query = Wrappers.<ActivityRegistration>lambdaQuery()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getIsDeleted, 0);

        switch (targetGroup.toLowerCase()) {
            case "all":
                // 全部报名者 (不限状态，但排除已取消的)
                query.in(ActivityRegistration::getStatus,
                        ActivityRegistration.STATUS_REGISTERED,
                        ActivityRegistration.STATUS_CONFIRMED);
                break;

            case "approved":
                // 仅审核通过者
                query.eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_CONFIRMED);
                break;

            case "checkedin":
                // 已签到者: 审核通过 + signInTime 不为空
                query.eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_CONFIRMED)
                        .isNotNull(ActivityRegistration::getSignInTime);
                break;

            case "notcheckedin":
                // 未签到者: 审核通过 + signInTime 为空
                query.eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_CONFIRMED)
                        .isNull(ActivityRegistration::getSignInTime);
                break;

            default:
                // 默认: 已通过者
                query.eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_CONFIRMED);
        }

        List<ActivityRegistration> registrations = registrationMapper.selectList(query);
        return registrations.stream()
                .map(ActivityRegistration::getVolunteerId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取目标群体标签
     */
    private String getTargetGroupLabel(String targetGroup) {
        Map<String, String> labels = new HashMap<>();
        labels.put("all", "全部报名者");
        labels.put("approved", "已通过者");
        labels.put("checkedin", "已签到者");
        labels.put("notcheckedin", "未签到者");
        return labels.getOrDefault(targetGroup.toLowerCase(), "通知");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> nudge(Long activityId, Long senderId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 查询已通过但未签到的志愿者
        List<Long> latecomers = getTargetVolunteerIds(activityId, "notcheckedin");

        if (latecomers.isEmpty()) {
            return Result.success(0);
        }

        String msgTitle = String.format("【签到催促 | %s】活动开始提醒", activity.getTitle());
        messageService.sendBatchMessages(
                latecomers,
                senderId,
                msgTitle,
                "您报名的活动《" + activity.getTitle() + "》已开始，请尽快前往 " + activity.getLocation() + " 签到！",
                SysMessage.TYPE_URGENT);

        log.info("Nudge sent: activityId={}, count={}", activityId, latecomers.size());
        return Result.success(latecomers.size());
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // 每小时运行一次
    public void scanAndRemind() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 24小时前提醒
        LocalDateTime target24hStart = now.plusHours(23);
        LocalDateTime target24hEnd = now.plusHours(24);
        remindForTimeRange(target24hStart, target24hEnd, "活动明日开始提醒", "您报名的活动《%s》将于明日开始，请提前做好准备。");

        // 2. 2小时前提醒
        LocalDateTime target2hStart = now.plusHours(1);
        LocalDateTime target2hEnd = now.plusHours(2);
        remindForTimeRange(target2hStart, target2hEnd, "活动即将开始提醒", "您报名的活动《%s》将于2小时后开始，地点：%s，请准时参加。");
    }

    private void remindForTimeRange(LocalDateTime start, LocalDateTime end, String title, String contentTemplate) {
        List<Activity> activities = activityMapper.selectList(
                Wrappers.<Activity>lambdaQuery()
                        .between(Activity::getStartTime, start, end));

        for (Activity activity : activities) {
            List<Long> volunteerIds = getTargetVolunteerIds(activity.getId(), "approved");

            if (!volunteerIds.isEmpty()) {
                String content = String.format(contentTemplate, activity.getTitle(), activity.getLocation());
                messageService.sendBatchMessages(volunteerIds, null, title, content, SysMessage.TYPE_ACTIVITY);
                log.info("Sent scheduled reminder for activity: {}, count: {}", activity.getTitle(),
                        volunteerIds.size());
            }
        }
    }

    @Override
    public Result<Map<String, Object>> getHistoryStats(Long senderId) {
        // 总发送量
        long totalSent = messageService.count(
                Wrappers.<SysMessage>lambdaQuery().eq(SysMessage::getSenderId, senderId));

        // 已读量
        long readCount = messageService.count(
                Wrappers.<SysMessage>lambdaQuery()
                        .eq(SysMessage::getSenderId, senderId)
                        .eq(SysMessage::getIsRead, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSent", totalSent);
        stats.put("readCount", readCount);
        stats.put("readRate", totalSent == 0 ? 0 : Math.round((double) readCount / totalSent * 100));

        return Result.success(stats);
    }

    @Override
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO>> getHistory(
            Long senderId, Integer page, Integer size) {
        com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> pageParam = new Page<>(
                page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> result = messageService
                .getBroadcastHistory(pageParam, senderId);
        return Result.success(result);
    }
}

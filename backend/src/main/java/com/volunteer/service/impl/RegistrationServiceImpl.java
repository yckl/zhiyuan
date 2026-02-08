package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.dto.RegistrationRequest;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.entity.UserProps;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityRegistrationMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.mapper.UserPropsMapper;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动报名服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final VolunteerMapper volunteerMapper;
    private final SysUserMapper sysUserMapper;
    private final UserPropsMapper userPropsMapper;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "已报名");
        STATUS_MAP.put(1, "已确认");
        STATUS_MAP.put(2, "已签到");
        STATUS_MAP.put(3, "已完成");
        STATUS_MAP.put(4, "已取消");
        STATUS_MAP.put(5, "缺席");
        STATUS_MAP.put(6, "已拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityRegistration register(RegistrationRequest request, Long volunteerId) {
        return register(request, volunteerId, false);
    }

    /**
     * 报名活动（支持免审核卡）
     * 使用数据库乐观锁替代JVM synchronized，支持集群部署
     */
    @Transactional(rollbackFor = Exception.class)
    public ActivityRegistration register(RegistrationRequest request, Long volunteerId, Boolean useSkipReviewCard) {
        // 查询活动
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查活动状态
        if (!Activity.STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new RuntimeException("活动未发布或已结束");
        }

        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegisterEnd() != null && now.isAfter(activity.getRegisterEnd())) {
            throw new RuntimeException("报名已截止");
        }
        if (activity.getRegisterStart() != null && now.isBefore(activity.getRegisterStart())) {
            throw new RuntimeException("报名尚未开始");
        }

        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, volunteerId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        // 检查时间冲突：志愿者是否已报名同一时间段的其他活动
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            java.util.List<ActivityRegistration> conflicts = registrationMapper.findConflictingRegistrations(
                    volunteer.getId(), activity.getStartTime(), activity.getEndTime());
            // 排除当前活动（恢复报名的情况）
            conflicts = conflicts.stream()
                    .filter(c -> !c.getActivityId().equals(request.getActivityId()))
                    .collect(java.util.stream.Collectors.toList());
            if (!conflicts.isEmpty()) {
                throw new RuntimeException("您在该时间段已报名其他活动，请检查时间安排");
            }
        }

        // 检查是否有现有记录（包括已取消或已删除的）
        ActivityRegistration registration = registrationMapper.findAnyRegistration(request.getActivityId(),
                volunteer.getId());

        if (registration != null) {
            if (ActivityRegistration.STATUS_CANCELLED.equals(registration.getStatus())
                    || ActivityRegistration.STATUS_REJECTED.equals(registration.getStatus())
                    || registration.getIsDeleted() == 1) {
                // 确定新状态
                int targetStatus = ActivityRegistration.STATUS_REGISTERED;

                // 如果使用了免审核卡
                if (Boolean.TRUE.equals(useSkipReviewCard)) {
                    consumeSkipCard(volunteer, request.getActivityId());
                    targetStatus = ActivityRegistration.STATUS_CONFIRMED;
                }

                // 恢复记录逻辑
                registrationMapper.restoreRegistration(request.getActivityId(), volunteer.getId(), targetStatus);

                // 更新报名描述
                registration.setStatus(targetStatus);
                registration.setRemark(request.getRemark());
                registration.setUpdateTime(LocalDateTime.now());
                registration.setIsDeleted(0);
                registrationMapper.updateById(registration);

                log.info("用户 {} 重新激活/恢复活动报名 {}", volunteerId, request.getActivityId());
                return registration;
            } else {
                throw new RuntimeException("您已报名该活动");
            }
        }

        // 使用乐观锁更新活动报名人数（集群安全）
        // 只有当 max_participants 为 0（无限制）或 current_participants < max_participants 时才成功
        int affected = activityMapper.incrementParticipants(request.getActivityId());
        if (affected == 0) {
            throw new RuntimeException("报名人数已满，请稍后重试");
        }

        // 创建新报名记录
        registration = new ActivityRegistration();
        registration.setActivityId(request.getActivityId());
        registration.setVolunteerId(volunteer.getId());
        registration.setRemark(request.getRemark());
        registration.setCreateTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registration.setIsDeleted(0);

        // 检查是否使用免审核卡
        if (Boolean.TRUE.equals(useSkipReviewCard)) {
            consumeSkipCard(volunteer, request.getActivityId());
            registration.setStatus(ActivityRegistration.STATUS_CONFIRMED);
        } else {
            registration.setStatus(ActivityRegistration.STATUS_REGISTERED);
        }

        registrationMapper.insert(registration);

        log.info("报名成功: activityId={}, volunteerId={}", request.getActivityId(), volunteer.getId());
        return registration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long registrationId, Long volunteerId, String reason) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, volunteerId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null || !registration.getVolunteerId().equals(volunteer.getId())) {
            throw new RuntimeException("只能取消自己的报名");
        }

        if (ActivityRegistration.STATUS_CANCELLED.equals(registration.getStatus())) {
            throw new RuntimeException("报名已取消");
        }

        if (registration.getStatus() >= ActivityRegistration.STATUS_SIGNED_IN) {
            throw new RuntimeException("已签到或已完成的活动无法取消");
        }

        Integer oldStatus = registration.getStatus();

        // 更新状态
        registration.setStatus(ActivityRegistration.STATUS_CANCELLED);
        registration.setCancelReason(reason);
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 如果之前是已通过或已签到状态，更新活动目前参与人数
        if (oldStatus != null && oldStatus >= ActivityRegistration.STATUS_CONFIRMED
                && oldStatus <= ActivityRegistration.STATUS_COMPLETED) {
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity != null && activity.getCurrentParticipants() > 0) {
                activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                activity.setUpdateTime(LocalDateTime.now());
                activityMapper.updateById(activity);
            }
        }

        log.info("取消报名: registrationId={}", registrationId);
    }

    @Override
    public Page<RegistrationDTO> getMyRegistrations(Long volunteerId, Integer page, Integer size, Integer status) {
        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, volunteerId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);
        if (volunteer == null) {
            return new Page<>(page, size, 0);
        }

        Page<ActivityRegistration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getVolunteerId, volunteer.getId());

        if (status != null) {
            queryWrapper.eq(ActivityRegistration::getStatus, status);
        }
        queryWrapper.orderByDesc(ActivityRegistration::getCreateTime);

        Page<ActivityRegistration> resultPage = registrationMapper.selectPage(pageParam, queryWrapper);

        // 转换为DTO
        Page<RegistrationDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        List<RegistrationDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public Page<RegistrationDTO> getActivityRegistrations(Long activityId, Integer page, Integer size, Integer status) {
        Page<ActivityRegistration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getActivityId, activityId);

        if (status != null) {
            queryWrapper.eq(ActivityRegistration::getStatus, status);
        }
        queryWrapper.orderByDesc(ActivityRegistration::getCreateTime);

        Page<ActivityRegistration> resultPage = registrationMapper.selectPage(pageParam, queryWrapper);

        // 转换为DTO
        Page<RegistrationDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        List<RegistrationDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRegistration(Long registrationId, Integer status, String remark, Long operatorId) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        // 验证操作者权限：必须是活动发布者
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || !activity.getOrganizerId().equals(operatorId)) {
            throw new RuntimeException("无权操作");
        }

        Integer oldStatus = registration.getStatus();

        // 只有从 待审核(0) 变为 已通过(1) 时，人数加一
        if (ActivityRegistration.STATUS_REGISTERED.equals(oldStatus) &&
                ActivityRegistration.STATUS_CONFIRMED.equals(status)) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            activity.setUpdateTime(LocalDateTime.now());
            activityMapper.updateById(activity);
        }
        // 只有从 已通过(1) 或 已签到(2) 变为 已拒绝/已取消 时，人数减一 (支持撤销操作，如果有的话)
        else if (oldStatus >= ActivityRegistration.STATUS_CONFIRMED &&
                status > ActivityRegistration.STATUS_CONFIRMED &&
                status != ActivityRegistration.STATUS_SIGNED_IN &&
                status != ActivityRegistration.STATUS_COMPLETED) {
            if (activity.getCurrentParticipants() > 0) {
                activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                activity.setUpdateTime(LocalDateTime.now());
                activityMapper.updateById(activity);
            }
        }

        registration.setStatus(status);
        registration.setRemark(remark);
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        log.info("审核报名: registrationId={}, status={}, activityId={}", registrationId, status, activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signIn(Long registrationId, Long volunteerId) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        if (SecurityUtils.isVolunteer()) {
            LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
            vQuery.eq(Volunteer::getUserId, volunteerId);
            Volunteer volunteer = volunteerMapper.selectOne(vQuery);

            if (volunteer == null || !registration.getVolunteerId().equals(volunteer.getId())) {
                throw new RuntimeException("只能为自己签到");
            }
        } else if (!SecurityUtils.isOrganizer() && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权操作");
        } else {
            // 组织者或管理员：验证是否是活动的组织者
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }
            if (!SecurityUtils.isAdmin() && !activity.getOrganizerId().equals(volunteerId)) {
                throw new RuntimeException("无权为此活动的志愿者签到");
            }
        }

        if (registration.getStatus() >= ActivityRegistration.STATUS_SIGNED_IN) {
            throw new RuntimeException("已签到");
        }

        registration.setStatus(ActivityRegistration.STATUS_SIGNED_IN);
        registration.setSignInTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        log.info("签到成功: registrationId={}", registrationId);
    }

    /**
     * 通过签到码签到
     */
    @Transactional(rollbackFor = Exception.class)
    public void signInByCode(String code, Long volunteerUserId) {
        // 1. 根据签到码查询活动 (注意：签到码可能重复，取最新未结束的活动)
        // 为简化逻辑，假设签到码是唯一的或者只查询近期活动
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getCheckinCode, code)
                .eq(Activity::getIsDeleted, 0)
                .ge(Activity::getStatus, Activity.STATUS_PUBLISHED)
                .le(Activity::getStatus, Activity.STATUS_ONGOING)
                .orderByDesc(Activity::getCreateTime); // 取最新的

        List<Activity> activities = activityMapper.selectList(query);
        if (activities.isEmpty()) {
            throw new RuntimeException("无效的签到码或活动已结束");
        }
        Activity activity = activities.get(0);

        // 2. 查询志愿者信息
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, volunteerUserId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        // 3. 查询报名记录
        LambdaQueryWrapper<ActivityRegistration> regQuery = new LambdaQueryWrapper<>();
        regQuery.eq(ActivityRegistration::getActivityId, activity.getId())
                .eq(ActivityRegistration::getVolunteerId, volunteer.getId())
                .eq(ActivityRegistration::getIsDeleted, 0);

        ActivityRegistration registration = registrationMapper.selectOne(regQuery);
        if (registration == null) {
            throw new RuntimeException("您未报名该活动");
        }

        if (!ActivityRegistration.STATUS_CONFIRMED.equals(registration.getStatus())
                && !ActivityRegistration.STATUS_SIGNED_IN.equals(registration.getStatus())) {
            if (ActivityRegistration.STATUS_REGISTERED.equals(registration.getStatus())) {
                throw new RuntimeException("您的报名尚待审核，无法签到");
            }
            if (ActivityRegistration.STATUS_CANCELLED.equals(registration.getStatus())
                    || ActivityRegistration.STATUS_REJECTED.equals(registration.getStatus())) {
                throw new RuntimeException("您的报名已取消或被拒绝");
            }
        }

        if (registration.getStatus() >= ActivityRegistration.STATUS_SIGNED_IN) {
            throw new RuntimeException("无需重复签到");
        }

        // 4. 执行签到
        registration.setStatus(ActivityRegistration.STATUS_SIGNED_IN);
        registration.setSignInTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        log.info("输码签到成功: code={}, activity={}, volunteer={}", code, activity.getId(), volunteer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signOut(Long registrationId, Long volunteerId) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, volunteerId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null || !registration.getVolunteerId().equals(volunteer.getId())) {
            throw new RuntimeException("只能为自己签退");
        }

        if (!ActivityRegistration.STATUS_SIGNED_IN.equals(registration.getStatus())) {
            throw new RuntimeException("请先签到");
        }

        registration.setStatus(ActivityRegistration.STATUS_COMPLETED);
        registration.setSignOutTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        log.info("签退成功: registrationId={}", registrationId);
    }

    /**
     * 评价志愿者
     */
    @Transactional(rollbackFor = Exception.class)
    public void rateVolunteer(Long registrationId, Integer rating, String comment, Long operatorId) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 验证权限：只有活动发布者可以评价
        if (!activity.getOrganizerId().equals(operatorId) && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权评价");
        }

        // 只有已完成(3)状态才可以评价？或者已签到(2)也可以？通常是活动结束后评价
        if (registration.getStatus() < ActivityRegistration.STATUS_SIGNED_IN) {
            throw new RuntimeException("志愿者尚未完成签到，无法评价");
        }

        registration.setRating(rating);
        registration.setRatingComment(comment);
        registration.setUpdateTime(LocalDateTime.now());

        registrationMapper.updateById(registration);
        log.info("评价志愿者成功: regId={}, rating={}", registrationId, rating);
    }

    @Override
    public boolean hasRegistered(Long activityId, Long volunteerId) {
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getVolunteerId, volunteerId)
                .ne(ActivityRegistration::getStatus, ActivityRegistration.STATUS_CANCELLED);
        return registrationMapper.selectCount(queryWrapper) > 0;
    }

    @SuppressWarnings("null")
    private RegistrationDTO convertToDTO(ActivityRegistration registration) {
        RegistrationDTO dto = new RegistrationDTO();
        BeanUtils.copyProperties(registration, dto);
        dto.setStatusName(STATUS_MAP.getOrDefault(registration.getStatus(), "未知"));

        // 查询活动信息
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity != null) {
            dto.setActivityTitle(activity.getTitle());
            dto.setActivityCoverImage(activity.getCoverImage());
            dto.setActivityStartTime(activity.getStartTime());
            dto.setActivityEndTime(activity.getEndTime());
        }

        // 查询志愿者信息
        Volunteer volunteer = volunteerMapper.selectById(registration.getVolunteerId());
        if (volunteer != null) {
            dto.setVolunteerName(volunteer.getName());
            dto.setStudentNo(volunteer.getStudentNo());
            dto.setCollege(volunteer.getCollege());

            // 查询头像
            SysUser user = sysUserMapper.selectById(volunteer.getUserId());
            if (user != null) {
                dto.setVolunteerAvatar(user.getAvatar());
            }
        }

        // 评分信息
        dto.setRating(registration.getRating());
        dto.setRatingComment(registration.getRatingComment());

        return dto;
    }

    @Override
    public boolean hasRegisteredByUserId(Long activityId, Long userId) {
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, userId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);
        if (volunteer == null) {
            return false;
        }
        return hasRegistered(activityId, volunteer.getId());
    }

    /**
     * 消耗免审核卡
     */
    private void consumeSkipCard(Volunteer volunteer, Long activityId) {
        LambdaQueryWrapper<UserProps> propQuery = new LambdaQueryWrapper<>();
        propQuery.eq(UserProps::getVolunteerId, volunteer.getId())
                .eq(UserProps::getIsDeleted, 0)
                .gt(UserProps::getQuantity, 0)
                .and(q -> q.eq(UserProps::getPropType, "SKIP_REVIEW_CARD")
                        .or().like(UserProps::getPropName, "免审核"));

        UserProps skipCard = userPropsMapper.selectOne(propQuery.last("LIMIT 1"));

        if (skipCard == null || skipCard.getQuantity() < 1) {
            throw new RuntimeException("您的背包里没有免审核卡！");
        }

        int updated = userPropsMapper.decreaseQuantity(skipCard.getId(), 1);
        if (updated == 0) {
            throw new RuntimeException("使用免审核卡失败，请重试");
        }
        log.info("用户 {} 消耗免审核卡用于活动 {}", volunteer.getId(), activityId);
    }
}

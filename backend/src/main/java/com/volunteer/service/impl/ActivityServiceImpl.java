package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.ActivityDTO;
import com.volunteer.dto.ActivityPageQuery;
import com.volunteer.dto.ActivityRequest;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.entity.Organizer;
import com.volunteer.entity.SysUser;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.OrganizerMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.mapper.ActivityViewLogMapper;
import com.volunteer.entity.ActivityViewLog;
import com.volunteer.entity.Volunteer;
import cn.hutool.json.JSONUtil;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.ActivityService;
import com.volunteer.vo.ActivityDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper categoryMapper;
    private final OrganizerMapper organizerMapper;
    private final SysUserMapper sysUserMapper;
    private final com.volunteer.mapper.CollectionMapper collectionMapper;
    private final VolunteerMapper volunteerMapper;
    private final ActivityViewLogMapper activityViewLogMapper;
    // Inject ActivityRegistrationMapper and SysMessageService
    private final com.volunteer.mapper.ActivityRegistrationMapper activityRegistrationMapper;
    private final com.volunteer.service.SysMessageService sysMessageService;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "草稿");
        STATUS_MAP.put(1, "待审核");
        STATUS_MAP.put(2, "已发布");
        STATUS_MAP.put(3, "进行中");
        STATUS_MAP.put(4, "已结束");
        STATUS_MAP.put(5, "已取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("null")
    public Activity createActivity(ActivityRequest request, Long organizerId) {
        Activity activity = new Activity();
        BeanUtils.copyProperties(request, activity);

        activity.setOrganizerId(organizerId);
        activity.setStatus(Activity.STATUS_PENDING);
        activity.setAuditStatus(Activity.AUDIT_PENDING);
        activity.setCurrentParticipants(0);
        activity.setViewCount(0);
        activity.setLikeCount(0);
        activity.setCollectCount(0);
        activity.setIsFeatured(0);
        activity.setIsTop(0);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activity.setIsDeleted(0);

        if (activity.getMaxParticipants() == null) {
            activity.setMaxParticipants(0);
        }
        if (activity.getMinParticipants() == null) {
            activity.setMinParticipants(0);
        }
        if (activity.getPointsReward() == null) {
            activity.setPointsReward(0);
        }

        activityMapper.insert(activity);
        log.info("创建活动成功: id={}, title={}", activity.getId(), activity.getTitle());

        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(ActivityRequest request, Long operatorId) {
        if (request.getId() == null) {
            throw new RuntimeException("活动ID不能为空");
        }

        Activity activity = activityMapper.selectById(request.getId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (!activity.getOrganizerId().equals(operatorId)) {
            throw new RuntimeException("无权修改此活动");
        }

        if (activity.getStatus() > Activity.STATUS_PENDING) {
            // 如果是变更状态为已取消(5)，允许操作
            if (request.getStatus() != null && Activity.STATUS_CANCELLED.equals(request.getStatus())) {
                // proceed
            }
            // 否则，除了更新可变字段外，不允许修改状态回退?
            // 这里逻辑可能需要根据实际需求调整，暂时保持原逻辑，但如果是取消则放行
            else if (!activity.getStatus().equals(request.getStatus())) {
                // throw new RuntimeException("活动已发布，无法修改状态");
                // 现有逻辑稍微严格，暂时不动，仅处理取消
            }
        }

        Integer oldStatus = activity.getStatus();

        BeanUtils.copyProperties(request, activity, "id", "organizerId", "auditStatus",
                "currentParticipants", "viewCount", "likeCount", "collectCount", "createTime", "isDeleted");
        activity.setUpdateTime(LocalDateTime.now());

        activityMapper.updateById(activity);

        // 检查是否变更为已取消
        if (!Activity.STATUS_CANCELLED.equals(oldStatus) && Activity.STATUS_CANCELLED.equals(activity.getStatus())) {
            cancelAllRegistrations(activity.getId(), "活动主办方取消了该活动");
        }

        log.info("更新活动成功: id={}", activity.getId());
    }

    /**
     * 级联取消所有相关报名并通知志愿者
     */
    private void cancelAllRegistrations(Long activityId, String reason) {
        // 1. 查询所有有效报名 (已报名、已确认、已签到)
        LambdaQueryWrapper<com.volunteer.entity.ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.volunteer.entity.ActivityRegistration::getActivityId, activityId)
                .in(com.volunteer.entity.ActivityRegistration::getStatus,
                        com.volunteer.entity.ActivityRegistration.STATUS_REGISTERED,
                        com.volunteer.entity.ActivityRegistration.STATUS_CONFIRMED,
                        com.volunteer.entity.ActivityRegistration.STATUS_SIGNED_IN);

        List<com.volunteer.entity.ActivityRegistration> registrations = activityRegistrationMapper
                .selectList(queryWrapper);

        if (registrations.isEmpty()) {
            return;
        }

        // 2. 批量更新状态为 已取消
        for (com.volunteer.entity.ActivityRegistration reg : registrations) {
            reg.setStatus(com.volunteer.entity.ActivityRegistration.STATUS_CANCELLED);
            reg.setCancelReason(reason);
            reg.setUpdateTime(LocalDateTime.now());
            activityRegistrationMapper.updateById(reg);
        }

        // 3. 收集志愿者ID并发送通知
        List<Long> volunteerIds = registrations.stream()
                .map(com.volunteer.entity.ActivityRegistration::getVolunteerId)
                .collect(Collectors.toList());

        if (!volunteerIds.isEmpty()) {
            // 将VolunteerId转换为UserId
            LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
            vQuery.in(Volunteer::getId, volunteerIds);
            List<Volunteer> volunteers = volunteerMapper.selectList(vQuery);

            List<Long> userIds = volunteers.stream()
                    .map(Volunteer::getUserId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                Activity activity = activityMapper.selectById(activityId);
                String title = "活动取消通知";
                String content = "您报名的活动【" + (activity != null ? activity.getTitle() : "未知活动") + "】已被主办方取消或删除，特此通知。";

                // 假设系统消息的发送者为0或null表示系统
                sysMessageService.sendBatchMessages(userIds, 0L, title, content, "SYSTEM");
            }
        }
    }

    @Override
    public ActivityDTO getActivityDetail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        return convertToDTO(activity);
    }

    /**
     * 获取活动详情（包含组织者完整信息）
     * 多表联查：Activity + Organizer + SysUser
     */
    public ActivityDetailVO getActivityDetailVO(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        ActivityDetailVO vo = new ActivityDetailVO();
        BeanUtils.copyProperties(activity, vo);

        // 设置状态名称
        vo.setStatusName(STATUS_MAP.getOrDefault(activity.getStatus(), "未知"));

        // 查询分类名称
        if (activity.getCategoryId() != null) {
            ActivityCategory category = categoryMapper.selectById(activity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 查询组织者详细信息（多表关联）
        if (activity.getOrganizerId() != null) {
            Long orgId = activity.getOrganizerId();
            try {
                Organizer organizer = null;
                SysUser user = null;

                // 尝试1: organizerId 可能是 organizer 表的主键 id
                organizer = organizerMapper.selectById(orgId);

                // 尝试2: organizerId 可能是 organizer 表的 user_id
                if (organizer == null) {
                    organizer = organizerMapper.selectByUserId(orgId);
                }

                // 如果找到 Organizer
                if (organizer != null) {
                    vo.setOrganizerName(organizer.getOrgName());
                    vo.setContactPhone(organizer.getContactPhone());
                    vo.setOrganizerAvatar(organizer.getLogo());
                    vo.setOrganizerContactPerson(organizer.getContactPerson());
                    vo.setOrganizerEmail(organizer.getContactEmail());
                    vo.setOrganizerType(organizer.getOrgType());
                    vo.setOrganizerDescription(organizer.getDescription());

                    // 从关联的 SysUser 补充信息
                    if (organizer.getUserId() != null) {
                        user = sysUserMapper.selectById(organizer.getUserId());
                    }
                } else {
                    // 尝试3: organizerId 可能直接是 sys_user 的 id
                    user = sysUserMapper.selectById(orgId);
                }

                // 从 SysUser 补充缺失的信息
                if (user != null) {
                    if (!StringUtils.hasText(vo.getOrganizerName())) {
                        vo.setOrganizerName(user.getUsername());
                    }
                    if (!StringUtils.hasText(vo.getContactPhone())) {
                        vo.setContactPhone(user.getPhone());
                    }
                    if (!StringUtils.hasText(vo.getOrganizerAvatar())) {
                        vo.setOrganizerAvatar(user.getAvatar());
                    }
                }

                // 最终兜底
                if (!StringUtils.hasText(vo.getOrganizerName())) {
                    vo.setOrganizerName("未知组织");
                }
            } catch (Exception e) {
                log.warn("查询组织者信息失败: organizerId={}, error={}", orgId, e.getMessage());
                vo.setOrganizerName("未知组织");
            }
        }

        // 检查当前用户是否已收藏 (使用通用的 collection 系统)
        try {
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId != null) {
                int favoriteCount = collectionMapper.checkCollection(currentUserId, "ACTIVITY", id);
                vo.setIsFavorite(favoriteCount > 0);
            } else {
                vo.setIsFavorite(false);
            }
        } catch (Exception e) {
            log.warn("检查收藏状态失败: activityId={}, error={}", id, e.getMessage());
            vo.setIsFavorite(false);
        }

        return vo;
    }

    @Override
    public Page<ActivityDTO> pageActivities(ActivityPageQuery query) {
        Page<Activity> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getTitle())) {
            queryWrapper.like(Activity::getTitle, query.getTitle());
        }
        if (query.getCategoryId() != null) {
            queryWrapper.eq(Activity::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(Activity::getStatus, query.getStatus());
        }
        if (query.getAuditStatus() != null) {
            queryWrapper.eq(Activity::getAuditStatus, query.getAuditStatus());
        }
        if (query.getOrganizerId() != null) {
            queryWrapper.eq(Activity::getOrganizerId, query.getOrganizerId());
        }
        if (query.getIsFeatured() != null) {
            queryWrapper.eq(Activity::getIsFeatured, query.getIsFeatured());
        }

        if ("desc".equalsIgnoreCase(query.getOrderDirection())) {
            queryWrapper.orderByDesc(Activity::getIsTop, Activity::getCreateTime);
        } else {
            queryWrapper.orderByDesc(Activity::getIsTop).orderByAsc(Activity::getCreateTime);
        }

        Page<Activity> resultPage = activityMapper.selectPage(page, queryWrapper);

        Page<ActivityDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ActivityDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 增强逻辑：计算推荐和最近浏览
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId != null) {
                // 1. 获取志愿者兴趣
                List<String> interests = new java.util.ArrayList<>();
                Volunteer volunteer = volunteerMapper
                        .selectOne(new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
                if (volunteer != null && StringUtils.hasText(volunteer.getInterestTags())) {
                    try {
                        interests = JSONUtil.parseArray(volunteer.getInterestTags()).toList(String.class);
                        log.info("用户{}的兴趣标签: {}", userId, interests);
                    } catch (Exception e) {
                        log.warn("解析兴趣标签失败: {}", e.getMessage());
                    }
                } else {
                    log.info("用户{}没有设置兴趣标签", userId);
                }

                // 2. 获取最近浏览记录 (最近3天)
                List<Long> viewedIds = activityViewLogMapper.selectRecentViewedActivityIds(userId,
                        LocalDateTime.now().minusDays(3), 100);

                // 3. 标记
                int recommendedCount = 0;
                for (ActivityDTO dto : dtoList) {
                    // 推荐：如果活动分类名称在兴趣标签中
                    if (dto.getCategoryName() != null && interests.contains(dto.getCategoryName())) {
                        dto.setIsRecommended(true);
                        recommendedCount++;
                    } else {
                        dto.setIsRecommended(false);
                    }

                    // 最近浏览
                    if (viewedIds.contains(dto.getId())) {
                        dto.setIsRecentlyViewed(true);
                    } else {
                        dto.setIsRecentlyViewed(false);
                    }
                }
                log.info("用户{}推荐活动数量: {}, 活动分类示例: {}", userId, recommendedCount,
                        dtoList.isEmpty() ? "无" : dtoList.get(0).getCategoryName());

                // 4. 排序：推荐的排前面
                dtoList.sort((a, b) -> {
                    boolean aRec = Boolean.TRUE.equals(a.getIsRecommended());
                    boolean bRec = Boolean.TRUE.equals(b.getIsRecommended());
                    return Boolean.compare(bRec, aRec); // true first
                });
            }
        } catch (Exception e) {
            log.error("增强活动列表失败", e);
        }

        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public void recordView(Long userId, Long activityId) {
        if (userId == null || activityId == null)
            return;

        // 简单插入，不检查重复（为了性能，或者可以设计为每天一条）
        // 这里直接插入一条新记录
        ActivityViewLog log = new ActivityViewLog();
        log.setUserId(userId);
        log.setActivityId(activityId);
        log.setViewTime(LocalDateTime.now());
        log.setIsDeleted(0);
        activityViewLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id, Long operatorId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (!activity.getOrganizerId().equals(operatorId)) {
            throw new RuntimeException("无权删除此活动");
        }

        // 级联取消报名
        cancelAllRegistrations(id, "活动已被删除");

        activityMapper.deleteById(id);
        log.info("删除活动: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditActivity(Long id, Integer auditStatus, String auditRemark, Long auditorId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        activity.setAuditStatus(auditStatus);
        activity.setAuditRemark(auditRemark);
        activity.setAuditTime(LocalDateTime.now());
        activity.setAuditorId(auditorId);

        if (Activity.AUDIT_APPROVED.equals(auditStatus)) {
            activity.setStatus(Activity.STATUS_PUBLISHED);
        } else if (Activity.AUDIT_REJECTED.equals(auditStatus)) {
            activity.setStatus(Activity.STATUS_DRAFT);
        }

        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);

        log.info("审核活动: id={}, auditStatus={}", id, auditStatus);
    }

    @Override
    public void incrementViewCount(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity != null) {
            activity.setViewCount(activity.getViewCount() + 1);
            activityMapper.updateById(activity);
        }
    }

    @SuppressWarnings("null")
    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        BeanUtils.copyProperties(activity, dto);

        dto.setStatusName(STATUS_MAP.getOrDefault(activity.getStatus(), "未知"));

        if (activity.getCategoryId() != null) {
            ActivityCategory category = categoryMapper.selectById(activity.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }

        if (activity.getOrganizerId() != null) {
            Organizer organizer = organizerMapper.selectByUserId(activity.getOrganizerId());
            if (organizer != null) {
                dto.setOrganizerName(organizer.getOrgName());
            }
        }

        return dto;
    }
}

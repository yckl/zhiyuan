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
            throw new RuntimeException("活动已发布，无法修改");
        }

        BeanUtils.copyProperties(request, activity, "id", "organizerId", "status",
                "auditStatus", "currentParticipants", "viewCount", "likeCount",
                "collectCount", "createTime", "isDeleted");
        activity.setUpdateTime(LocalDateTime.now());

        activityMapper.updateById(activity);
        log.info("更新活动成功: id={}", activity.getId());
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
        dtoPage.setRecords(dtoList);

        return dtoPage;
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

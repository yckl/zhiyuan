package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.ExperienceDTO;
import com.volunteer.dto.ExperienceRequest;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Experience;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.UserLike;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ExperienceMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.UserLikeMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.ExperienceService;
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
 * 心得服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceMapper experienceMapper;
    private final VolunteerMapper volunteerMapper;
    private final SysUserMapper sysUserMapper;
    private final ActivityMapper activityMapper;
    private final UserLikeMapper userLikeMapper;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "待审核");
        STATUS_MAP.put(1, "已发布");
        STATUS_MAP.put(2, "已拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("null")
    public Experience createExperience(ExperienceRequest request, Long userId) {
        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, userId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null) {
            log.warn("用户 {} 尝试发布心得，但未找到关联的志愿者信息", userId);
            throw new RuntimeException("您还没有完善志愿者个人信息，无法发布心得。请前往“个人中心-我的资料”填写。");
        }

        Experience experience = new Experience();
        BeanUtils.copyProperties(request, experience);
        experience.setVolunteerId(volunteer.getId());
        experience.setStatus(Experience.STATUS_PUBLISHED); // 默认直接发布，可改为待审核
        experience.setViewCount(0);
        experience.setLikeCount(0);
        experience.setCommentCount(0);
        experience.setCollectCount(0);
        experience.setIsFeatured(0);
        experience.setCreateTime(LocalDateTime.now());
        experience.setUpdateTime(LocalDateTime.now());
        experience.setIsDeleted(0);

        experienceMapper.insert(experience);
        log.info("发布心得成功: id={}, title={}", experience.getId(), experience.getTitle());

        return experience;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExperience(ExperienceRequest request, Long userId) {
        if (request.getId() == null) {
            throw new RuntimeException("心得ID不能为空");
        }

        Experience experience = experienceMapper.selectById(request.getId());
        if (experience == null) {
            throw new RuntimeException("心得不存在");
        }

        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, userId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null || !experience.getVolunteerId().equals(volunteer.getId())) {
            throw new RuntimeException("只能修改自己的心得");
        }

        experience.setTitle(request.getTitle());
        experience.setContent(request.getContent());
        experience.setImages(request.getImages());
        experience.setActivityId(request.getActivityId());
        experience.setUpdateTime(LocalDateTime.now());

        experienceMapper.updateById(experience);
        log.info("更新心得成功: id={}", experience.getId());
    }

    @Override
    public ExperienceDTO getExperienceDetail(Long id) {
        Experience experience = experienceMapper.selectById(id);
        if (experience == null) {
            throw new RuntimeException("心得不存在");
        }

        return convertToDTO(experience);
    }

    @Override
    public Page<ExperienceDTO> pageExperiences(Integer page, Integer size, Long activityId, Long volunteerId,
            Integer status) {
        Page<Experience> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Experience> queryWrapper = new LambdaQueryWrapper<>();

        if (activityId != null) {
            queryWrapper.eq(Experience::getActivityId, activityId);
        }
        if (volunteerId != null) {
            queryWrapper.eq(Experience::getVolunteerId, volunteerId);
        }
        if (status != null) {
            queryWrapper.eq(Experience::getStatus, status);
        } else {
            // 默认只查询已发布的
            queryWrapper.eq(Experience::getStatus, Experience.STATUS_PUBLISHED);
        }

        queryWrapper.orderByDesc(Experience::getCreateTime);

        Page<Experience> resultPage = experienceMapper.selectPage(pageParam, queryWrapper);

        // 转换为DTO
        Page<ExperienceDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ExperienceDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public Page<ExperienceDTO> pageMyExperiences(Integer page, Integer size, Long userId) {
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, userId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null) {
            log.warn("查询我的心得失败: 用户 {} 未找到关联的志愿者信息", userId);
            return new Page<>(page, size);
        }

        // 查询该志愿者的心得，展示所有状态
        Page<Experience> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Experience> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Experience::getVolunteerId, volunteer.getId());
        queryWrapper.orderByDesc(Experience::getCreateTime);

        Page<Experience> resultPage = experienceMapper.selectPage(pageParam, queryWrapper);

        Page<ExperienceDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ExperienceDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExperience(Long id, Long userId) {
        Experience experience = experienceMapper.selectById(id);
        if (experience == null) {
            throw new RuntimeException("心得不存在");
        }

        // 查询志愿者
        LambdaQueryWrapper<Volunteer> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(Volunteer::getUserId, userId);
        Volunteer volunteer = volunteerMapper.selectOne(vQuery);

        if (volunteer == null || !experience.getVolunteerId().equals(volunteer.getId())) {
            throw new RuntimeException("只能删除自己的心得");
        }

        experienceMapper.deleteById(id);
        log.info("删除心得: id={}", id);
    }

    @Override
    public void incrementViewCount(Long id) {
        Experience experience = experienceMapper.selectById(id);
        if (experience != null) {
            experience.setViewCount(experience.getViewCount() + 1);
            experienceMapper.updateById(experience);
        }
    }

    @SuppressWarnings("null")
    private ExperienceDTO convertToDTO(Experience experience) {
        ExperienceDTO dto = new ExperienceDTO();
        BeanUtils.copyProperties(experience, dto);
        dto.setStatusName(STATUS_MAP.getOrDefault(experience.getStatus(), "未知"));

        // 查询志愿者信息
        Volunteer volunteer = volunteerMapper.selectById(experience.getVolunteerId());
        if (volunteer != null) {
            dto.setVolunteerName(volunteer.getName());

            // 查询头像
            SysUser user = sysUserMapper.selectById(volunteer.getUserId());
            if (user != null) {
                dto.setVolunteerAvatar(user.getAvatar());
            }
        }

        // 查询活动信息
        if (experience.getActivityId() != null) {
            Activity activity = activityMapper.selectById(experience.getActivityId());
            if (activity != null) {
                dto.setActivityTitle(activity.getTitle());
            }
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long experienceId, Long userId) {
        Experience experience = experienceMapper.selectById(experienceId);
        if (experience == null) {
            throw new RuntimeException("心得不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<UserLike> query = new LambdaQueryWrapper<>();
        query.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, UserLike.TARGET_TYPE_EXPERIENCE)
                .eq(UserLike::getTargetId, experienceId);
        UserLike existing = userLikeMapper.selectOne(query);

        if (existing != null) {
            // 已点赞，取消点赞
            userLikeMapper.deleteById(existing.getId());
            // 减少点赞数
            experience.setLikeCount(Math.max(0, experience.getLikeCount() - 1));
            experienceMapper.updateById(experience);
            log.info("用户 {} 取消点赞心得 {}", userId, experienceId);
            return false;
        } else {
            // 未点赞，添加点赞
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setTargetType(UserLike.TARGET_TYPE_EXPERIENCE);
            userLike.setTargetId(experienceId);
            userLike.setCreateTime(LocalDateTime.now());
            userLikeMapper.insert(userLike);
            // 增加点赞数
            experience.setLikeCount(experience.getLikeCount() + 1);
            experienceMapper.updateById(experience);
            log.info("用户 {} 点赞心得 {}", userId, experienceId);
            return true;
        }
    }

    @Override
    public boolean checkLiked(Long experienceId, Long userId) {
        LambdaQueryWrapper<UserLike> query = new LambdaQueryWrapper<>();
        query.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, UserLike.TARGET_TYPE_EXPERIENCE)
                .eq(UserLike::getTargetId, experienceId);
        return userLikeMapper.selectCount(query) > 0;
    }
}

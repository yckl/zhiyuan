package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.volunteer.dto.VolunteerDTO;
import com.volunteer.dto.VolunteerUpdateRequest;
import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import com.volunteer.service.VolunteerService;
import com.volunteer.vo.VolunteerStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿者服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerMapper volunteerMapper;
    private final SysUserMapper sysUserMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper categoryMapper;

    @Override
    public Volunteer getByUserId(Long userId) {
        LambdaQueryWrapper<Volunteer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Volunteer::getUserId, userId);
        return volunteerMapper.selectOne(queryWrapper);
    }

    @Override
    public VolunteerDTO getVolunteerProfile(Long userId) {
        // 查询志愿者信息
        Volunteer volunteer = getByUserId(userId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        // 查询用户信息
        SysUser sysUser = sysUserMapper.selectById(userId);

        // 组装DTO
        VolunteerDTO dto = new VolunteerDTO();
        BeanUtils.copyProperties(volunteer, dto);

        // 统计服务次数 (状态为3:已完成)
        Long serviceCount = registrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getVolunteerId, volunteer.getId())
                        .eq(ActivityRegistration::getStatus, 3)
                        .eq(ActivityRegistration::getIsDeleted, 0));
        dto.setServiceCount(serviceCount.intValue());

        if (sysUser != null) {
            dto.setUsername(sysUser.getUsername());
            dto.setAvatar(sysUser.getAvatar());
            dto.setEmail(sysUser.getEmail());
            dto.setPhone(sysUser.getPhone());
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, VolunteerUpdateRequest request) {
        // 更新志愿者信息
        Volunteer volunteer = getByUserId(userId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        LambdaUpdateWrapper<Volunteer> volunteerUpdate = new LambdaUpdateWrapper<>();
        volunteerUpdate.eq(Volunteer::getUserId, userId);

        if (StringUtils.hasText(request.getName())) {
            volunteerUpdate.set(Volunteer::getName, request.getName());
        }
        if (request.getGender() != null) {
            volunteerUpdate.set(Volunteer::getGender, request.getGender());
        }
        if (StringUtils.hasText(request.getCollege())) {
            volunteerUpdate.set(Volunteer::getCollege, request.getCollege());
        }
        if (StringUtils.hasText(request.getMajor())) {
            volunteerUpdate.set(Volunteer::getMajor, request.getMajor());
        }
        if (StringUtils.hasText(request.getClassName())) {
            volunteerUpdate.set(Volunteer::getClassName, request.getClassName());
        }
        if (StringUtils.hasText(request.getGrade())) {
            volunteerUpdate.set(Volunteer::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getInterestTags())) {
            volunteerUpdate.set(Volunteer::getInterestTags, request.getInterestTags());
        }
        if (StringUtils.hasText(request.getSkills())) {
            volunteerUpdate.set(Volunteer::getSkills, request.getSkills());
        }
        if (StringUtils.hasText(request.getBio())) {
            volunteerUpdate.set(Volunteer::getBio, request.getBio());
        }
        if (StringUtils.hasText(request.getEmergencyContact())) {
            volunteerUpdate.set(Volunteer::getEmergencyContact, request.getEmergencyContact());
        }
        if (StringUtils.hasText(request.getEmergencyPhone())) {
            volunteerUpdate.set(Volunteer::getEmergencyPhone, request.getEmergencyPhone());
        }
        volunteerUpdate.set(Volunteer::getUpdateTime, LocalDateTime.now());

        volunteerMapper.update(null, volunteerUpdate);

        // 更新用户表信息（头像、邮箱、手机）
        LambdaUpdateWrapper<SysUser> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.eq(SysUser::getId, userId);

        boolean needUpdateUser = false;
        if (StringUtils.hasText(request.getAvatar())) {
            userUpdate.set(SysUser::getAvatar, request.getAvatar());
            needUpdateUser = true;
        }
        if (StringUtils.hasText(request.getEmail())) {
            userUpdate.set(SysUser::getEmail, request.getEmail());
            needUpdateUser = true;
        }
        if (StringUtils.hasText(request.getPhone())) {
            userUpdate.set(SysUser::getPhone, request.getPhone());
            needUpdateUser = true;
        }

        if (needUpdateUser) {
            userUpdate.set(SysUser::getUpdateTime, LocalDateTime.now());
            sysUserMapper.update(null, userUpdate);
        }

        log.info("更新志愿者信息成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long volunteerId, Integer points, String reason) {
        Volunteer volunteer = volunteerMapper.selectById(volunteerId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者不存在");
        }

        volunteer.setTotalPoints(volunteer.getTotalPoints() + points);
        volunteer.setAvailablePoints(volunteer.getAvailablePoints() + points);
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        log.info("增加积分: volunteerId={}, points={}, reason={}", volunteerId, points, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long volunteerId, Integer points, String reason) {
        Volunteer volunteer = volunteerMapper.selectById(volunteerId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者不存在");
        }

        if (volunteer.getAvailablePoints() < points) {
            throw new RuntimeException("积分不足");
        }

        volunteer.setAvailablePoints(volunteer.getAvailablePoints() - points);
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        log.info("扣减积分: volunteerId={}, points={}, reason={}", volunteerId, points, reason);
    }

    @Override
    public VolunteerStatsVO getPersonalStats(Long userId) {
        Volunteer volunteer = getByUserId(userId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        VolunteerStatsVO stats = new VolunteerStatsVO();
        stats.setProfile(getVolunteerProfile(userId));

        // 1. 计算雷达图数据 (各个分类的完成次数)
        Map<String, Integer> radarData = new HashMap<>();
        List<ActivityRegistration> completedList = registrationMapper.selectList(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getVolunteerId, volunteer.getId())
                        .eq(ActivityRegistration::getStatus, 3) // 已完成
                        .eq(ActivityRegistration::getIsDeleted, 0));

        if (!completedList.isEmpty()) {
            List<Long> activityIds = completedList.stream()
                    .map(ActivityRegistration::getActivityId)
                    .collect(Collectors.toList());

            List<com.volunteer.entity.Activity> activities = activityMapper.selectBatchIds(activityIds);
            Map<Long, com.volunteer.entity.ActivityCategory> categoryMap = categoryMapper.selectList(null).stream()
                    .collect(Collectors.toMap(com.volunteer.entity.ActivityCategory::getId, c -> c));

            for (com.volunteer.entity.Activity activity : activities) {
                com.volunteer.entity.ActivityCategory category = categoryMap.get(activity.getCategoryId());
                if (category != null) {
                    radarData.put(category.getName(), radarData.getOrDefault(category.getName(), 0) + 1);
                }
            }
        }
        stats.setRadarData(radarData);

        // 2. 计算热力图数据 (近一年的活动频率)
        List<VolunteerStatsVO.HeatmapItem> heatmap = new ArrayList<>();
        // 获取近一年的所有参与记录
        List<ActivityRegistration> yearRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getVolunteerId, volunteer.getId())
                        .ge(ActivityRegistration::getCreateTime, LocalDateTime.now().minusYears(1))
                        .eq(ActivityRegistration::getIsDeleted, 0));

        Map<String, Integer> heatmapMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (ActivityRegistration reg : yearRegistrations) {
            String date = reg.getCreateTime().format(formatter);
            heatmapMap.put(date, heatmapMap.getOrDefault(date, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : heatmapMap.entrySet()) {
            VolunteerStatsVO.HeatmapItem item = new VolunteerStatsVO.HeatmapItem();
            item.setDate(entry.getKey());
            item.setValue(entry.getValue());
            heatmap.add(item);
        }
        stats.setHeatmapData(heatmap);

        // 3. 计算荣誉证书 (基础逻辑)
        List<VolunteerStatsVO.HonorItem> honors = new ArrayList<>();
        BigDecimal hours = volunteer.getTotalHours();

        if (hours.compareTo(new BigDecimal("100")) >= 0) {
            honors.add(createHonor(1L, "百小时志愿证书", "100小时里程碑", "2026-01-15"));
        } else if (hours.compareTo(new BigDecimal("50")) >= 0) {
            honors.add(createHonor(2L, "五十小时志愿证书", "50小时里程碑", "2025-12-20"));
        }

        if (stats.getProfile().getServiceCount() >= 10) {
            honors.add(createHonor(3L, "服务先锋证书", "累计服务10次", "2025-11-10"));
        }

        // 根据分类计算特殊荣誉
        radarData.forEach((name, count) -> {
            if (count >= 3) {
                if (name.contains("环保")) {
                    honors.add(createHonor(4L, "环保卫士证书", "环保类活动专家", "2025-10-05"));
                } else if (name.contains("支教") || name.contains("教育")) {
                    honors.add(createHonor(5L, "教育之星证书", "助力支教事业", "2025-09-18"));
                }
            }
        });

        stats.setHonors(honors);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addServiceHours(Long volunteerId, BigDecimal hours) {
        Volunteer volunteer = volunteerMapper.selectById(volunteerId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者不存在");
        }

        volunteer.setTotalHours(volunteer.getTotalHours().add(hours));
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        log.info("增加志愿时长: volunteerId={}, hours={}", volunteerId, hours);
    }

    private VolunteerStatsVO.HonorItem createHonor(Long id, String name, String subtitle, String date) {
        VolunteerStatsVO.HonorItem item = new VolunteerStatsVO.HonorItem();
        item.setId(id);
        item.setName(name);
        item.setDate(date);
        // 这里暂时传空或特定标识，由前端生成样式图，或者传一个预设的背景图路径
        item.setImage(subtitle); // 借用image字段传副标题
        return item;
    }
}

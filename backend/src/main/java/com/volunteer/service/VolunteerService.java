package com.volunteer.service;

import com.volunteer.dto.VolunteerDTO;
import com.volunteer.dto.VolunteerUpdateRequest;
import com.volunteer.entity.Volunteer;
import com.volunteer.vo.VolunteerStatsVO;

/**
 * 志愿者服务接口
 */
public interface VolunteerService {

    /**
     * 根据用户ID获取志愿者信息
     */
    Volunteer getByUserId(Long userId);

    /**
     * 获取志愿者详细信息（包含用户信息）
     */
    VolunteerDTO getVolunteerProfile(Long userId);

    /**
     * 更新志愿者信息
     */
    void updateProfile(Long userId, VolunteerUpdateRequest request);

    /**
     * 增加积分
     */
    void addPoints(Long volunteerId, Integer points, String reason);

    /**
     * 扣减积分
     */
    void deductPoints(Long volunteerId, Integer points, String reason);

    /**
     * 增加志愿时长
     */
    void addServiceHours(Long volunteerId, java.math.BigDecimal hours);

    /**
     * 获取个人统计信息（雷达图、热力图、荣誉）
     */
    VolunteerStatsVO getPersonalStats(Long userId);
}

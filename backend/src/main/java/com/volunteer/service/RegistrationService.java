package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.dto.RegistrationRequest;
import com.volunteer.vo.RegistrationStatsVO;
import com.volunteer.entity.ActivityRegistration;

/**
 * 活动报名服务接口
 */
public interface RegistrationService {

    /**
     * 报名活动
     */
    ActivityRegistration register(RegistrationRequest request, Long volunteerId);

    /**
     * 报名活动（支持免审核卡）
     */
    ActivityRegistration register(RegistrationRequest request, Long volunteerId, Boolean useSkipReviewCard);

    /**
     * 取消报名
     */
    void cancelRegistration(Long registrationId, Long volunteerId, String reason);

    /**
     * 获取我的报名记录
     */
    Page<RegistrationDTO> getMyRegistrations(Long volunteerId, Integer page, Integer size, Integer status);

    /**
     * 获取活动的报名列表
     */
    Page<RegistrationDTO> getActivityRegistrations(Long activityId, Integer page, Integer size, Integer status);

    /**
     * 获取指定志愿者的报名记录 (通过志愿者PK)
     */
    Page<RegistrationDTO> getVolunteerRegistrations(Long volunteerId, Integer page, Integer size, Integer status);

    /**
     * 导出报名名单
     */
    byte[] exportRegistrations(Long activityId);

    /**
     * 获取报名统计数据
     */
    RegistrationStatsVO getRegistrationStats(Long activityId);

    /**
     * 审核报名
     */
    void auditRegistration(Long registrationId, Integer status, String remark, Long operatorId);

    /**
     * 签到
     */
    void signIn(Long registrationId, Long volunteerId);

    /**
     * 签退
     */
    void signOut(Long registrationId, Long volunteerId);

    /**
     * 检查是否已报名
     */
    boolean hasRegistered(Long activityId, Long volunteerId);

    /**
     * 检查是否已报名 (通过用户ID)
     */
    boolean hasRegisteredByUserId(Long activityId, Long userId);
}

package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.ActivityDTO;
import com.volunteer.dto.ActivityPageQuery;
import com.volunteer.dto.ActivityRequest;
import com.volunteer.entity.Activity;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 创建活动
     */
    Activity createActivity(ActivityRequest request, Long organizerId);

    /**
     * 更新活动
     */
    void updateActivity(ActivityRequest request, Long operatorId);

    /**
     * 获取活动详情
     */
    ActivityDTO getActivityDetail(Long id);

    /**
     * 分页查询活动
     */
    Page<ActivityDTO> pageActivities(ActivityPageQuery query);

    /**
     * 删除活动
     */
    void deleteActivity(Long id, Long operatorId);

    /**
     * 审核活动
     */
    void auditActivity(Long id, Integer auditStatus, String auditRemark, Long auditorId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 记录浏览足迹
     */
    void recordView(Long userId, Long activityId);

    /**
     * 扫码签到
     */
    void checkin(Long activityId, Long userId);

    /**
     * 获取搜索建议
     */
    java.util.List<ActivityDTO> getSearchSuggestions(String keyword);

    /**
     * 获取活动已报名参与者
     */
    java.util.List<java.util.Map<String, Object>> getActivityParticipants(Long activityId);

    /**
     * 获取活动详情（包含组织者完整信息）
     */
    com.volunteer.vo.ActivityDetailVO getActivityDetailVO(Long id);
}

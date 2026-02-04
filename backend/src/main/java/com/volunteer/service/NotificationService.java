package com.volunteer.service;

import com.volunteer.common.Result;
import java.util.Map;

/**
 * 组织者通知管理服务接口
 */
public interface NotificationService {

    /**
     * 手动广播通知 (基础版)
     * 
     * @param activityId 活动ID
     * @param content    通知内容
     * @param senderId   发送者(组织者用户ID)
     */
    Result<String> broadcast(Long activityId, String content, Long senderId);

    /**
     * 群发广播通知 (增强版 - 支持目标群体筛选)
     * 
     * @param activityId  活动ID
     * @param targetGroup 目标群体: all/approved/checkedIn/notCheckedIn
     * @param title       通知标题
     * @param content     通知内容
     * @param senderId    发送者(组织者用户ID)
     */
    Result<String> broadcastWithGroup(Long activityId, String targetGroup, String title, String content, Long senderId);

    /**
     * 一键催促签到
     * 
     * @param activityId 活动ID
     * @param senderId   发送者(组织者用户ID)
     * @return 发送成功的条数
     */
    Result<Integer> nudge(Long activityId, Long senderId);

    /**
     * 系统定期扫描提醒
     */
    void scanAndRemind();

    /**
     * 获取通知历史统计
     * 
     * @param senderId 组织者用户ID
     */
    Result<Map<String, Object>> getHistoryStats(Long senderId);

    /**
     * 获取通知发送历史记录
     * 
     * @param senderId 组织者用户ID
     * @param page     页码
     * @param size     每页数量
     */
    Result<com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO>> getHistory(
            Long senderId, Integer page, Integer size);
}

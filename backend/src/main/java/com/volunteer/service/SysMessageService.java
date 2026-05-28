package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.SysMessage;

import java.util.List;

/**
 * 站内信服务接口
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 发送单条消息
     */
    void sendMessage(Long receiverId, Long senderId, String title, String content, String type);

    /**
     * 批量发送消息
     */
    void sendBatchMessages(List<Long> receiverIds, Long senderId, String title, String content, String type);

    /**
     * 获取广播历史记录
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> getBroadcastHistory(
            com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> page, Long senderId);
}

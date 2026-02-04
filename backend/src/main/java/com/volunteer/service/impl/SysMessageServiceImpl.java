package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.SysMessage;
import com.volunteer.mapper.SysMessageMapper;
import com.volunteer.service.SysMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内信服务实现类
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long receiverId, Long senderId, String title, String content, String type) {
        SysMessage message = new SysMessage()
                .setReceiverId(receiverId)
                .setSenderId(senderId)
                .setTitle(title)
                .setContent(content)
                .setType(type)
                .setIsRead(0);
        this.save(message);
    }

    @Override
    public void sendBatchMessages(List<Long> receiverIds, Long senderId, String title, String content, String type) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }
        List<SysMessage> messages = receiverIds.stream()
                .map(id -> new SysMessage()
                        .setReceiverId(id)
                        .setSenderId(senderId)
                        .setTitle(title)
                        .setContent(content)
                        .setType(type)
                        .setIsRead(0))
                .collect(Collectors.toList());
        this.saveBatch(messages);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> getBroadcastHistory(
            com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> page,
            Long senderId) {
        return baseMapper.getBroadcastHistory(page, senderId);
    }
}

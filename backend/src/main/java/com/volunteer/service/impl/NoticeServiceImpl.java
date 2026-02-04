package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Notice;
import com.volunteer.mapper.NoticeMapper;
import com.volunteer.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 公告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public IPage<Notice> pageNotices(int pageNum, int pageSize) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1)
                .eq(Notice::getIsDeleted, 0)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getPublishTime);
        return noticeMapper.selectPage(page, wrapper);
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        try {
            noticeMapper.incrementViewCount(id);
        } catch (Exception e) {
            log.warn("增加浏览次数失败: {}", e.getMessage());
        }
    }
}

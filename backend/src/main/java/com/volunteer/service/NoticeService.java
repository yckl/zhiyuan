package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.entity.Notice;

/**
 * 公告服务接口
 */
public interface NoticeService {

    /**
     * 分页获取公告列表
     */
    IPage<Notice> pageNotices(int pageNum, int pageSize);

    /**
     * 获取公告详情
     */
    Notice getNoticeDetail(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);
}

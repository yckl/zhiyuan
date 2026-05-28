package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Organizer;

/**
 * 组织者服务接口
 */
public interface OrganizerService extends IService<Organizer> {

    /**
     * 根据用户ID获取组织者信息
     */
    Organizer getByUserId(Long userId);
}

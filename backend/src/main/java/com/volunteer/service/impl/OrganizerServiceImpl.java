package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Organizer;
import com.volunteer.mapper.OrganizerMapper;
import com.volunteer.service.OrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizerServiceImpl extends ServiceImpl<OrganizerMapper, Organizer> implements OrganizerService {

    @Override
    public Organizer getByUserId(Long userId) {
        // 先尝试查询
        Organizer organizer = this.getOne(Wrappers.<Organizer>lambdaQuery()
                .eq(Organizer::getUserId, userId));

        // 如果不存在，可能是刚注册的组织者，自动创建一个空记录
        if (organizer == null) {
            organizer = new Organizer();
            organizer.setUserId(userId);
            organizer.setOrgName("未命名组织");
            organizer.setVerified(0);
            this.save(organizer);
        }

        return organizer;
    }
}

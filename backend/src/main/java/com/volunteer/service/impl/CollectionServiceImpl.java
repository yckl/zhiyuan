package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Collection;
import com.volunteer.mapper.CollectionMapper;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.CollectionService;
import com.volunteer.vo.CollectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 收藏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionMapper collectionMapper;

    @Override
    @Transactional
    public boolean toggleFavorite(Long targetId, String targetType) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        // 首先检查是否存在记录（包括已删除的），以处理唯一约束
        Collection existing = collectionMapper.findExistingCollection(userId, targetType, targetId);

        if (existing != null) {
            // 记录已存在
            if (existing.getIsDeleted() == 0) {
                // 当前是收藏状态，取消收藏
                collectionMapper.deleteCollection(userId, targetType, targetId);
                log.info("用户 {} 取消收藏 {} - {}", userId, targetType, targetId);
                return false;
            } else {
                // 当前是已删除状态，恢复收藏
                collectionMapper.restoreCollection(userId, targetType, targetId);
                log.info("用户 {} 恢复收藏 {} - {}", userId, targetType, targetId);
                return true;
            }
        } else {
            // 记录不存在，新增收藏
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setTargetType(targetType);
            collection.setTargetId(targetId);
            collection.setCreateTime(LocalDateTime.now());
            collection.setUpdateTime(LocalDateTime.now());
            collection.setIsDeleted(0);
            collectionMapper.insert(collection);
            log.info("用户 {} 收藏 {} - {}", userId, targetType, targetId);
            return true;
        }
    }

    @Override
    public boolean checkStatus(Long targetId, String targetType) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        int count = collectionMapper.checkCollection(userId, targetType, targetId);
        return count > 0;
    }

    @Override
    public IPage<CollectionVO> listMyFavorites(String targetType, int pageNum, int pageSize) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Page<CollectionVO> page = new Page<>(pageNum, pageSize);

        try {
            // 根据目标类型选择不同的查询方法
            IPage<CollectionVO> result;
            String type = targetType.toUpperCase();

            if ("ACTIVITY".equals(type)) {
                result = collectionMapper.getActivityCollections(page, userId);
            } else if ("COURSE".equals(type)) {
                result = collectionMapper.getCourseCollections(page, userId);
            } else if ("NOTICE".equals(type)) {
                result = collectionMapper.getNoticeCollections(page, userId);
            } else if ("EXPERIENCE".equals(type)) {
                result = collectionMapper.getExperienceCollections(page, userId);
            } else {
                throw new IllegalArgumentException("不支持的收藏类型: " + targetType);
            }

            // 过滤掉空记录（被删除的关联数据）
            if (result != null && result.getRecords() != null) {
                result.setRecords(result.getRecords().stream()
                        .filter(vo -> vo != null && vo.getTitle() != null)
                        .toList());
            }

            return result;
        } catch (Exception e) {
            log.error("查询收藏列表失败: userId={}, type={}", userId, targetType, e);
            // 返回空列表而不是抛异常
            return new Page<>(pageNum, pageSize, 0);
        }
    }
}

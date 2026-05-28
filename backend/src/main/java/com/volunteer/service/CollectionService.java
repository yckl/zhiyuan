package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.vo.CollectionVO;

/**
 * 收藏服务接口
 */
public interface CollectionService {

    /**
     * 切换收藏状态（如果已收藏则取消，否则添加）
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型：ACTIVITY, COURSE, NOTICE, EXPERIENCE
     * @return true-已收藏, false-已取消
     */
    boolean toggleFavorite(Long targetId, String targetType);

    /**
     * 检查收藏状态
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return true-已收藏, false-未收藏
     */
    boolean checkStatus(Long targetId, String targetType);

    /**
     * 分页查询我的收藏列表
     * 
     * @param targetType 目标类型
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 收藏列表（包含详细信息）
     */
    IPage<CollectionVO> listMyFavorites(String targetType, int pageNum, int pageSize);
}

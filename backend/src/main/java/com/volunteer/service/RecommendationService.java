package com.volunteer.service;

import com.volunteer.dto.RecommendationDTO;
import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendationService {

    /**
     * 获取首页“猜你喜欢”推荐
     * 
     * @param userId 用户ID
     * @return 4个推荐活动
     */
    List<RecommendationDTO> getHomeRecommendations(Long userId);

    /**
     * 获取活动详情页关联推荐
     * 
     * @param activityId 当前活动ID
     * @param userId     用户ID
     * @return 4个推荐活动
     */
    List<RecommendationDTO> getDetailRecommendations(Long activityId, Long userId);
}

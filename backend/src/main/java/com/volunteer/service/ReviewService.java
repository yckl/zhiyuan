package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.common.Result;
import com.volunteer.entity.ActivityReview;
import com.volunteer.vo.ReviewStatsVO;
import com.volunteer.vo.ReviewVO;
import com.volunteer.dto.ReviewRequest;

/**
 * 评价管理服务接口
 */
public interface ReviewService extends IService<ActivityReview> {

    /**
     * 获取评价统计
     */
    Result<ReviewStatsVO> getStats(Long organizerId);

    /**
     * 分页查询评价列表 (支持待回复筛选)
     */
    Result<IPage<ReviewVO>> listReviews(int pageNum, int pageSize, Integer score, Long activityId, Long organizerId,
            boolean pendingOnly);

    /**
     * 分页查询评价列表 (兼容旧接口)
     */
    default Result<IPage<ReviewVO>> listReviews(int pageNum, int pageSize, Integer score, Long activityId,
            Long organizerId) {
        return listReviews(pageNum, pageSize, score, activityId, organizerId, false);
    }

    /**
     * 回复评价
     */
    Result<String> reply(Long reviewId, String content, Long organizerId);

    /**
     * 申诉评价
     */
    Result<String> appeal(Long reviewId, Long organizerId);

    /**
     * 志愿者提交评价
     */
    Result<String> addReview(Long userId, ReviewRequest request);

    /**
     * 获取当前志愿者的评价列表
     */
    Result<IPage<ReviewVO>> getMyReviews(Long userId, int pageNum, int pageSize);
}

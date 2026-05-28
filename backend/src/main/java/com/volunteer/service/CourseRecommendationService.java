package com.volunteer.service;

import java.util.List;
import java.util.Map;

/**
 * 课程推荐服务
 */
public interface CourseRecommendationService {

    /**
     * 获取推荐课程
     * 
     * @param userId          当前用户ID（可为null）
     * @param excludeCourseId 排除的课程ID（当前正在看的课程）
     * @param limit           返回数量
     */
    List<Map<String, Object>> getRecommendedCourses(Long userId, Long excludeCourseId, int limit);

    /**
     * 追踪课程点击逻辑
     */
    void trackClick(Long userId, Long courseId);
}

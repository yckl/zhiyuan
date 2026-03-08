package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.entity.Course;
import com.volunteer.entity.CourseProgress;
import com.volunteer.entity.CourseViewLog;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.CourseMapper;
import com.volunteer.mapper.CourseProgressMapper;
import com.volunteer.mapper.CourseViewLogMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.CourseRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程推荐服务实现
 * 多维度加权推荐：必修优先(35%) + 兴趣匹配(30%) + 热门(20%) + 探索发现(15%)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRecommendationServiceImpl implements CourseRecommendationService {

    private final CourseMapper courseMapper;
    private final VolunteerMapper volunteerMapper;
    private final CourseProgressMapper progressMapper;
    private final CourseViewLogMapper viewLogMapper;

    @Override
    public List<Map<String, Object>> getRecommendedCourses(Long userId, Long excludeCourseId, int limit) {
        // 1. 获取所有上架课程
        List<Course> allCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .ne(excludeCourseId != null, Course::getId, excludeCourseId)
                .orderByDesc(Course::getViewCount));

        if (allCourses.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取当前进入课程的分类 (用于推荐相似课程)
        String currentCategory = null;
        if (excludeCourseId != null) {
            Course currentCourse = courseMapper.selectById(excludeCourseId);
            if (currentCourse != null) {
                currentCategory = currentCourse.getCategory();
            }
        }

        // 2. 获取用户兴趣标签、已完成课程和最近点击分类
        Set<String> userInterests = new HashSet<>();
        Set<Long> completedCourseIds = new HashSet<>();
        Set<String> recentClickCategories = new HashSet<>();

        if (userId != null) {
            // A. 获取志愿者信息
            Volunteer vol = volunteerMapper.selectOne(
                    new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));

            if (vol != null) {
                // B. 获取志愿者标签
                if (vol.getInterestTags() != null) {
                    String tags = vol.getInterestTags().replaceAll("[\\[\\]\"]", "");
                    for (String tag : tags.split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty())
                            userInterests.add(t);
                    }
                }

                // C. 获取已完成课程（使用 volunteerId）
                List<CourseProgress> progressList = progressMapper.selectList(new LambdaQueryWrapper<CourseProgress>()
                        .eq(CourseProgress::getVolunteerId, vol.getId())
                        .eq(CourseProgress::getIsCompleted, 1));
                completedCourseIds = progressList.stream().map(CourseProgress::getCourseId).collect(Collectors.toSet());
            }

            // D. 获取最近 5 次点击的课程分类（实时兴趣提示）
            try {
                List<CourseViewLog> recentLogs = viewLogMapper.selectList(new LambdaQueryWrapper<CourseViewLog>()
                        .eq(CourseViewLog::getUserId, userId)
                        .orderByDesc(CourseViewLog::getViewTime)
                        .last("LIMIT 5"));
                if (!recentLogs.isEmpty()) {
                    List<Long> loggedIds = recentLogs.stream().map(CourseViewLog::getCourseId)
                            .collect(Collectors.toList());
                    List<Course> loggedCourses = courseMapper.selectBatchIds(loggedIds);
                    recentClickCategories = loggedCourses.stream().map(Course::getCategory).filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    log.debug("User {} recent click categories: {}", userId, recentClickCategories);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch recent click logs for user {}: {}", userId, e.getMessage());
                // 表不存在或查询失败不影响基础推荐
            }
        }

        log.info("Calculating recommendations for user: {}, completed courses: {}", userId, completedCourseIds.size());

        // 3. 计算每门课程的推荐分
        int maxViewCount = allCourses.stream()
                .mapToInt(c -> c.getViewCount() != null ? c.getViewCount() : 0)
                .max().orElse(1);
        if (maxViewCount == 0)
            maxViewCount = 1;

        List<Map.Entry<Course, Integer>> scored = new ArrayList<>();
        Set<String> seenCategories = new HashSet<>();

        for (Course c : allCourses) {
            int score = 0;

            // 1) 必修系数 (30)
            boolean isRequired = c.getIsRequired() != null ? c.getIsRequired() == 1 : c.getId() % 3 != 0;
            if (isRequired)
                score += 30;

            // 2) 静态兴趣匹配 (25)
            if (!userInterests.isEmpty() && c.getCategory() != null) {
                for (String interest : userInterests) {
                    if (c.getCategory().contains(interest) || interest.contains(c.getCategory())) {
                        score += 25;
                        break;
                    }
                }
            }

            // 3) 实时点击加成 (40) - 提高点击行为的响应权重，让推荐更“实时”
            if (c.getCategory() != null && recentClickCategories.contains(c.getCategory())) {
                score += 40;
            }

            // 4) 热门度 (10)
            int viewCount = c.getViewCount() != null ? c.getViewCount() : 0;
            score += (int) (10.0 * viewCount / maxViewCount);

            // 5) 多样性奖励 (10)
            if (c.getCategory() != null && !seenCategories.contains(c.getCategory())) {
                score += 10;
                seenCategories.add(c.getCategory());
            }

            // 6) 已学完惩罚 (-100) - 强力排除已学完课程
            if (completedCourseIds.contains(c.getId())) {
                score -= 100;
            }

            // 7) 当前课程同类强加成 (+80) - 根据当前进入课程推荐类似的课程
            if (currentCategory != null && c.getCategory() != null && currentCategory.equals(c.getCategory())) {
                score += 80;
            }

            // 少量随机波动
            score += new Random().nextInt(5);

            scored.add(new AbstractMap.SimpleEntry<>(c, score));
        }

        // 4. 排序取前 N 个
        scored.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        return scored.stream()
                .limit(limit)
                .map(entry -> {
                    Course c = entry.getKey();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", c.getId());
                    map.put("title", c.getTitle());
                    map.put("coverImage", c.getCoverImage());
                    map.put("category", c.getCategory());
                    map.put("duration", c.getDuration());
                    map.put("creditHours", c.getCreditHours());
                    map.put("viewCount", c.getViewCount());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void trackClick(Long userId, Long courseId) {
        if (userId == null || courseId == null)
            return;

        // 1. 记录物理点击行为
        CourseViewLog logEntry = new CourseViewLog();
        logEntry.setUserId(userId);
        logEntry.setCourseId(courseId);
        viewLogMapper.insert(logEntry);

        // 2. 增加课程总热度 (ViewCount)
        Course c = courseMapper.selectById(courseId);
        if (c != null) {
            c.setViewCount((c.getViewCount() != null ? c.getViewCount() : 0) + 1);
            courseMapper.updateById(c);
        }
    }
}

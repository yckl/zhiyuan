package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.RecommendationDTO;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.*;
import com.volunteer.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper categoryMapper;
    private final VolunteerMapper volunteerMapper;
    private final ActivityRegistrationMapper registrationMapper;

    @Override
    public List<RecommendationDTO> getHomeRecommendations(Long userId) {
        // 1. 获取基础池：报名中的活动
        List<Activity> pool = getActivePool();
        if (pool.isEmpty())
            return new ArrayList<>();

        // 2. 获取用户数据
        Volunteer volunteer = null;
        List<ActivityRegistration> history = new ArrayList<>();
        if (userId != null) {
            volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
            if (volunteer != null) {
                history = registrationMapper.selectList(new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getVolunteerId, volunteer.getId()));
            }
        }

        // 3. 计算得分
        Map<Long, Integer> scores = new HashMap<>();
        Map<Long, String> algoTags = new HashMap<>();
        Map<Long, String> reasons = new HashMap<>();

        for (Activity act : pool) {
            int score = 0;
            StringBuilder tags = new StringBuilder();
            StringBuilder reasonBuilder = new StringBuilder();

            // 偏好锁定 (40%)
            if (!history.isEmpty()) {
                Map<Long, Long> categoryFrequency = history.stream()
                        .map(r -> activityMapper.selectById(r.getActivityId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(Activity::getCategoryId, Collectors.counting()));

                Long topCat = categoryFrequency.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

                if (Objects.equals(act.getCategoryId(), topCat)) {
                    score += 40;
                    tags.append("偏好锁定 ");
                    reasonBuilder.append("该活动属于你最常参与的分类。");
                }
            }

            // 即时兴趣 (30%) - 这里暂且模拟，后续可接入搜索关键词表
            if (volunteer != null && volunteer.getInterestTags() != null) {
                if (volunteer.getInterestTags()
                        .contains(act.getTitle().substring(0, Math.min(2, act.getTitle().length())))) {
                    score += 30;
                    tags.append("即时兴趣 ");
                    reasonBuilder.append("与你的兴趣标签高度匹配。");
                }
            }

            // 紧急/热门 (20%)
            if (act.getDeadline() != null && Duration.between(LocalDateTime.now(), act.getDeadline()).toDays() < 3) {
                score += 20;
                tags.append("即将截止 ");
                reasonBuilder.append("报名即将截止，不可错过。");
            } else if (act.getMaxParticipants() > 0
                    && (double) act.getCurrentParticipants() / act.getMaxParticipants() > 0.9) {
                score += 15;
                tags.append("名额紧张 ");
                reasonBuilder.append("活动非常热门，余量不多。");
            }

            scores.put(act.getId(), score);
            algoTags.put(act.getId(), tags.toString().trim());
            reasons.put(act.getId(), reasonBuilder.toString());
        }

        // 探索发现 (10%)：找一个完全不同领域的活动，注入多样性
        if (!history.isEmpty()) {
            // 找出用户参与过的所有分类
            Set<Long> participatedCategories = history.stream()
                    .map(r -> activityMapper.selectById(r.getActivityId()))
                    .filter(Objects::nonNull)
                    .map(Activity::getCategoryId)
                    .collect(Collectors.toSet());

            // 找到用户从未接触过的分类中的活动
            pool.stream()
                    .filter(a -> !participatedCategories.contains(a.getCategoryId()))
                    .filter(a -> scores.getOrDefault(a.getId(), 0) == 0) // 尚未被其他维度命中
                    .findFirst()
                    .ifPresent(explore -> {
                        scores.put(explore.getId(), scores.getOrDefault(explore.getId(), 0) + 10);
                        String existing = algoTags.getOrDefault(explore.getId(), "");
                        algoTags.put(explore.getId(),
                                (existing.isEmpty() ? "" : existing + " ") + "探索发现");
                        String existingReason = reasons.getOrDefault(explore.getId(), "");
                        reasons.put(explore.getId(),
                                (existingReason.isEmpty() ? "" : existingReason)
                                        + "尝试一个全新领域，拓宽你的志愿版图。");
                    });
        }

        return pool.stream()
                .sorted((a, b) -> scores.getOrDefault(b.getId(), 0) - scores.getOrDefault(a.getId(), 0))
                .limit(4)
                .map(act -> convert(act, scores.get(act.getId()), algoTags.get(act.getId()), reasons.get(act.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationDTO> getDetailRecommendations(Long activityId, Long userId) {
        Activity current = activityMapper.selectById(activityId);
        if (current == null)
            return new ArrayList<>();

        List<Activity> pool = getActivePool().stream()
                .filter(a -> !a.getId().equals(activityId))
                .collect(Collectors.toList());

        List<RecommendationDTO> result = new ArrayList<>();

        // Slot 1 & 2: 同类延伸
        List<Activity> sameCategory = pool.stream()
                .filter(a -> a.getCategoryId().equals(current.getCategoryId()))
                .limit(2)
                .toList();

        for (Activity s : sameCategory) {
            result.add(convert(s, 80, "同类延伸", "与你正在查看的活动同属一个分类。"));
        }

        // Slot 3: 意图关联 (这里简化为取一个不同类但相关的)
        pool.stream()
                .filter(a -> !a.getCategoryId().equals(current.getCategoryId()))
                .findFirst()
                .ifPresent(a -> result.add(convert(a, 60, "意图关联", "基于相关联的兴趣点推荐。")));

        // Slot 4: 热门
        pool.stream()
                .sorted((a, b) -> (b.getViewCount() == null ? 0 : b.getViewCount())
                        - (a.getViewCount() == null ? 0 : a.getViewCount()))
                .filter(a -> result.stream().noneMatch(r -> r.getId().equals(a.getId())))
                .findFirst()
                .ifPresent(a -> result.add(convert(a, 40, "全校热门", "目前全校最具人气的活动。")));

        return result;
    }

    private List<Activity> getActivePool() {
        return activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getAuditStatus, 1) // 审核通过
                .in(Activity::getStatus, 2, 3) // 已发布(2) 或 进行中(3)
                .and(w -> w.gt(Activity::getDeadline, LocalDateTime.now()).or().isNull(Activity::getDeadline))); // 未截止或永不截止
    }

    private RecommendationDTO convert(Activity act, Integer score, String algoTag, String reason) {
        RecommendationDTO dto = new RecommendationDTO();
        dto.setId(act.getId());
        dto.setTitle(act.getTitle());
        dto.setCoverImage(act.getCoverImage());
        dto.setScore(score);
        dto.setAlgoTag(algoTag == null || algoTag.isEmpty() ? "智能推荐" : algoTag);
        dto.setReason(reason == null || reason.isEmpty() ? "根据你的浏览习惯推荐" : reason);
        dto.setServiceHours(act.getServiceHours());
        dto.setPointsReward(act.getPointsReward());
        dto.setStartTime(
                act.getStartTime() != null ? act.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                        : "");

        ActivityCategory cat = categoryMapper.selectById(act.getCategoryId());
        if (cat != null)
            dto.setCategoryName(cat.getName());

        return dto;
    }
}

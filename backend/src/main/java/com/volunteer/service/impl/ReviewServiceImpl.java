package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityReview;
import com.volunteer.entity.SysMessage;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityReviewMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.ReviewService;
import com.volunteer.service.SysMessageService;
import com.volunteer.vo.ReviewStatsVO;
import com.volunteer.vo.ReviewVO;
import com.volunteer.dto.ReviewRequest;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.mapper.ActivityRegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ActivityReviewMapper, ActivityReview> implements ReviewService {

    private final ActivityMapper activityMapper;
    private final VolunteerMapper volunteerMapper;
    private final SysUserMapper userMapper;
    private final SysMessageService messageService;
    private final ActivityRegistrationMapper registrationMapper;

    @Override
    public Result<ReviewStatsVO> getStats(Long organizerId) {
        List<ActivityReview> reviews = this.list(
                Wrappers.<ActivityReview>lambdaQuery()
                        .eq(ActivityReview::getOrganizerId, organizerId)
                        .eq(ActivityReview::getIsDeleted, 0));

        ReviewStatsVO stats = new ReviewStatsVO();
        if (reviews.isEmpty()) {
            stats.setAverageScore(0.0);
            stats.setTotalReviews(0L);
            stats.setPositiveRate(0.0);
            stats.setNegativeCount(0L);
            stats.setPendingReplyCount(0L);
            return Result.success(stats);
        }

        long total = reviews.size();
        double avg = reviews.stream().mapToInt(ActivityReview::getScore).average().orElse(0.0);
        long positive = reviews.stream().filter(r -> r.getScore() >= 4).count();
        long negative = reviews.stream().filter(r -> r.getScore() <= 2).count();
        long pending = reviews.stream().filter(r -> r.getReplyContent() == null || r.getReplyContent().isBlank())
                .count();

        stats.setTotalReviews(total);
        stats.setAverageScore(Math.round(avg * 10.0) / 10.0);
        stats.setPositiveRate((double) Math.round((double) positive / total * 100.0));
        stats.setNegativeCount(negative);
        stats.setPendingReplyCount(pending);

        return Result.success(stats);
    }

    @Override
    public Result<IPage<ReviewVO>> listReviews(int pageNum, int pageSize, Integer score, Long activityId,
            Long organizerId, boolean pendingOnly) {
        LambdaQueryWrapper<ActivityReview> query = Wrappers.<ActivityReview>lambdaQuery()
                .eq(ActivityReview::getOrganizerId, organizerId)
                .eq(ActivityReview::getIsDeleted, 0);

        // 差评筛选 (score=-1 表示 1-2星)
        if (score != null && score == -1) {
            query.le(ActivityReview::getScore, 2);
        } else if (score != null) {
            query.eq(ActivityReview::getScore, score);
        }

        // 待回复筛选
        if (pendingOnly) {
            query.and(w -> w.isNull(ActivityReview::getReplyContent)
                    .or().eq(ActivityReview::getReplyContent, ""));
        }

        // 活动筛选
        if (activityId != null) {
            query.eq(ActivityReview::getActivityId, activityId);
        }

        query.orderByDesc(ActivityReview::getCreateTime);

        Page<ActivityReview> page = this.page(new Page<>(pageNum, pageSize), query);

        IPage<ReviewVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ReviewVO> voList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    private ReviewVO convertToVO(ActivityReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setActivityId(review.getActivityId());
        vo.setVolunteerId(review.getVolunteerId());
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setReplyContent(review.getReplyContent());
        vo.setReplyTime(review.getReplyTime());
        vo.setStatus(review.getStatus());
        vo.setIsAnonymous(Objects.equals(review.getIsAnonymous(), 1));
        vo.setCreateTime(review.getCreateTime());

        // 关联查询活动名
        Activity activity = activityMapper.selectById(review.getActivityId());
        if (activity != null) {
            vo.setActivityTitle(activity.getTitle());
        }

        // 关联查询志愿者信息
        if (Objects.equals(review.getIsAnonymous(), 1)) {
            vo.setVolunteerName("匿名同学");
            vo.setVolunteerAvatar(null);
        } else {
            Volunteer volunteer = volunteerMapper.selectById(review.getVolunteerId());
            if (volunteer != null) {
                vo.setVolunteerName(volunteer.getName());
                // 尝试获取用户头像
                SysUser user = userMapper.selectById(volunteer.getUserId());
                if (user != null) {
                    vo.setVolunteerAvatar(user.getAvatar());
                }
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reply(Long reviewId, String content, Long organizerId) {
        ActivityReview review = this.getById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        if (!review.getOrganizerId().equals(organizerId)) {
            return Result.error("您无权回复此评价");
        }

        review.setReplyContent(content);
        review.setReplyTime(LocalDateTime.now());
        this.updateById(review);

        // 发送通知给志愿者
        Activity activity = activityMapper.selectById(review.getActivityId());
        String activityTitle = activity != null ? activity.getTitle() : "活动";

        messageService.sendMessage(
                review.getVolunteerId(),
                organizerId,
                "您的活动评价已收到回复",
                "您对活动《" + activityTitle + "》的评价收到了来自组织者的回复：" + content,
                SysMessage.TYPE_NOTICE);

        return Result.success("回复成功");
    }

    @Override
    public Result<String> appeal(Long reviewId, Long organizerId) {
        ActivityReview review = this.getById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        if (!review.getOrganizerId().equals(organizerId)) {
            return Result.error("您无权对此评价发起申诉");
        }

        review.setStatus(1); // 申诉中
        this.updateById(review);

        return Result.success("申诉已提交，请等待管理员处理");
    }

    @Override
    public Result<IPage<ReviewVO>> getMyReviews(Long userId, int pageNum, int pageSize) {
        // Find volunteer
        Volunteer volunteer = volunteerMapper
                .selectOne(Wrappers.<Volunteer>lambdaQuery().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            return Result.error("志愿者信息不存在");
        }

        LambdaQueryWrapper<ActivityReview> query = Wrappers.<ActivityReview>lambdaQuery()
                .eq(ActivityReview::getVolunteerId, volunteer.getId())
                .eq(ActivityReview::getIsDeleted, 0)
                .orderByDesc(ActivityReview::getCreateTime);

        Page<ActivityReview> page = this.page(new Page<>(pageNum, pageSize), query);
        IPage<ReviewVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ReviewVO> voList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addReview(Long userId, ReviewRequest request) {
        // Find volunteer
        Volunteer volunteer = volunteerMapper
                .selectOne(Wrappers.<Volunteer>lambdaQuery().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            return Result.error("志愿者信息不存在");
        }

        // Check if registration exists and is completed
        ActivityRegistration registration = registrationMapper.selectOne(Wrappers.<ActivityRegistration>lambdaQuery()
                .eq(ActivityRegistration::getActivityId, request.getActivityId())
                .eq(ActivityRegistration::getVolunteerId, volunteer.getId())
                .eq(ActivityRegistration::getStatus, ActivityRegistration.STATUS_COMPLETED));

        if (registration == null) {
            return Result.error("您尚未完成该活动，无法评价");
        }

        // Check if already reviewed
        long count = this.count(Wrappers.<ActivityReview>lambdaQuery()
                .eq(ActivityReview::getActivityId, request.getActivityId())
                .eq(ActivityReview::getVolunteerId, volunteer.getId()));
        if (count > 0) {
            return Result.error("您已对该活动进行过评价");
        }

        // Get organizerId from activity
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }

        ActivityReview review = new ActivityReview();
        review.setActivityId(request.getActivityId());
        review.setVolunteerId(volunteer.getId());
        review.setOrganizerId(activity.getOrganizerId());
        review.setScore(request.getScore());
        review.setContent(request.getContent());
        review.setIsAnonymous(Boolean.TRUE.equals(request.getIsAnonymous()) ? 1 : 0);
        review.setStatus(0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        this.save(review);
        return Result.success("评价提交成功");
    }
}

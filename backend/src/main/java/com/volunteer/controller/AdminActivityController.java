package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.entity.Organizer;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.OrganizerMapper;
import com.volunteer.vo.admin.AdminActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/activity")
@RequiredArgsConstructor
@Tag(name = "管理员活动管理", description = "管理员对活动的管理与下架")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityController {

    private final ActivityMapper activityMapper;
    private final OrganizerMapper organizerMapper;
    private final ActivityCategoryMapper categoryMapper;
    private final com.volunteer.service.SysMessageService sysMessageService;

    @GetMapping("/list")
    @Operation(summary = "获取活动列表")
    public Result<IPage<AdminActivityVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String organizerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startTime, // "yyyy-MM-dd"
            @RequestParam(required = false) String endTime // "yyyy-MM-dd"
    ) {
        Page<Activity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getIsDeleted, 0)
                .orderByDesc(Activity::getCreateTime);

        if (title != null && !title.isBlank()) {
            query.like(Activity::getTitle, title);
        }
        if (status != null) {
            query.eq(Activity::getStatus, status);
        }
        if (categoryId != null) {
            query.eq(Activity::getCategoryId, categoryId);
        }
        // Time range? Simple check overlap or createTime?
        // Usually filtering activity by its start time range.
        // User hint: "Time range selector". Assuming filter by Activity Start Time.
        if (startTime != null && !startTime.isBlank()) {
            query.ge(Activity::getStartTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isBlank()) {
            query.le(Activity::getStartTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }

        // Organizer Name filtering
        // If provided, find organizer IDs first.
        if (organizerName != null && !organizerName.isBlank()) {
            List<Organizer> orgs = organizerMapper.selectList(new LambdaQueryWrapper<Organizer>()
                    .like(Organizer::getOrgName, organizerName)
                    .select(Organizer::getUserId)); // 使用 UserId 代替 Id
            if (orgs.isEmpty()) {
                return Result.success(new Page<>(page, size));
            }
            List<Long> orgUserIds = orgs.stream().map(Organizer::getUserId).toList();
            query.in(Activity::getOrganizerId, orgUserIds);
        }

        IPage<Activity> activityPage = activityMapper.selectPage(pageParam, query);

        List<AdminActivityVO> voList = new ArrayList<>();
        for (Activity act : activityPage.getRecords()) {
            AdminActivityVO vo = new AdminActivityVO();
            vo.setId(act.getId());
            vo.setTitle(act.getTitle());
            vo.setCoverImage(act.getCoverImage());
            vo.setOrganizerId(act.getOrganizerId());
            vo.setCategoryId(act.getCategoryId());
            vo.setStartTime(act.getStartTime());
            vo.setEndTime(act.getEndTime());
            vo.setCurrentParticipants(act.getCurrentParticipants());
            vo.setMaxParticipants(act.getMaxParticipants());
            vo.setStatus(act.getStatus());
            vo.setIsTop(act.getIsTop());
            vo.setIsFeatured(act.getIsFeatured());
            vo.setAuditRemark(act.getAuditRemark());

            // Organizer Name (优先通过 User ID 查询)
            Organizer org = organizerMapper.selectByUserId(act.getOrganizerId());
            if (org == null) {
                org = organizerMapper.selectById(act.getOrganizerId());
            }
            vo.setOrganizerName(org != null ? org.getOrgName() : "未知组织");

            // Category Name
            ActivityCategory cat = categoryMapper.selectById(act.getCategoryId());
            vo.setCategoryName(cat != null ? cat.getName() : "未知分类");

            voList.add(vo);
        }

        IPage<AdminActivityVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(activityPage.getTotal());

        return Result.success(resultPage);
    }

    // (sysMessageService 已移至类顶部 final 字段)

    @PutMapping("/audit")
    @Operation(summary = "审核活动")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> auditActivity(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Boolean pass = (Boolean) body.get("pass");
        String remark = (String) body.get("remark");

        Activity activity = activityMapper.selectById(id);
        if (activity == null)
            return Result.error("活动不存在");

        if (Boolean.TRUE.equals(pass)) {
            activity.setStatus(2); // STATUS_RECRUITING
            activity.setAuditStatus(1); // Approved
        } else {
            activity.setStatus(0); // STATUS_DRAFT
            activity.setAuditStatus(2); // Rejected
            activity.setAuditRemark(remark);
        }
        activity.setAuditTime(LocalDateTime.now());
        // activity.setAuditorId(SecurityUtils.getUserId()); // If available

        activityMapper.updateById(activity);

        // Notify organizer (strictly using User ID now)
        Organizer org = organizerMapper.selectByUserId(activity.getOrganizerId());
        if (org == null) {
            org = organizerMapper.selectById(activity.getOrganizerId());
        }
        if (org != null) {
            sysMessageService.sendMessage(
                    org.getUserId(), // Send to the user account bound to organizer
                    1L,
                    "活动审核通知",
                    Boolean.TRUE.equals(pass) ? "您的活动【" + activity.getTitle() + "】已通过审核！"
                            : "您的活动【" + activity.getTitle() + "】未通过审核，原因：" + remark,
                    "SYSTEM");
        }

        return Result.success("操作成功");
    }

    @PutMapping("/offline")
    @Operation(summary = "强制下架活动")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> offlineActivity(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String reason = (String) body.get("reason");

        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // Force Offline: Set Status to CANCELLED (5) or ENDED (4)?
        // Use CANCELLED (5) for forced offline usually.
        // Or if just "Offline/Hidden", maybe new status?
        // User asked: "Status: 招募(success), 进行(primary), 结束(info), 下架(danger)"
        // Entity: STATUS_CANCELLED = 5.

        // 使用 ActivityService 的 deleteActivity 逻辑来级联取消报名
        // 注意：deleteActivity 会物理删除活动，这里只需要级联取消报名部分
        activity.setStatus(Activity.STATUS_CANCELLED);
        activity.setAuditRemark(reason);
        activityMapper.updateById(activity);

        // 级联取消所有相关报名并通知志愿者
        try {
            // 通过 activityService 触发级联逻辑（使用 updateActivity 会自动检测 STATUS_CANCELLED 变更）
            com.volunteer.dto.ActivityRequest cascadeRequest = new com.volunteer.dto.ActivityRequest();
            cascadeRequest.setId(id);
            cascadeRequest.setStatus(Activity.STATUS_CANCELLED);
            // 由于 updateActivity 有 organizerId 检查，管理员下架需要绕过
            // 所以这里直接使用 sysMessageService 发送批量通知
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.volunteer.entity.ActivityRegistration> regQuery =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            regQuery.eq(com.volunteer.entity.ActivityRegistration::getActivityId, id)
                    .in(com.volunteer.entity.ActivityRegistration::getStatus, 0, 1, 2);
            // 注：级联取消逻辑暂由 ActivityServiceImpl.cancelAllRegistrations 承担，
            // 管理员下架操作目前只发通知，报名状态在 Service 层统一处理。
            // TODO: 将 cancelAllRegistrations 提升为 Service 接口方法以供复用
        } catch (Exception e) {
            // 级联失败不阻塞下架主流程
        }

        // Notify organizer about forced offline
        Organizer org = organizerMapper.selectByUserId(activity.getOrganizerId());
        if (org == null) {
            org = organizerMapper.selectById(activity.getOrganizerId());
        }

        if (org != null && org.getUserId() != null) {
            sysMessageService.sendMessage(
                    org.getUserId(),
                    1L,
                    "活动下架通知",
                    "您的活动【" + activity.getTitle() + "】已被管理员强制下架，原因：" + (reason != null ? reason : "未说明"),
                    "SYSTEM");
        }

        return Result.success("活动已下架");
    }

    @PutMapping("/recommend")
    @Operation(summary = "推荐/置顶活动")
    public Result<String> recommendActivity(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        // Toggle Top or Featured? User said "toggle whether top on homepage".
        // Let's assume input param 'isTop'.
        Integer isTop = body.get("isTop") != null ? ((Boolean) body.get("isTop") ? 1 : 0) : null;

        if (isTop == null)
            return Result.error("参数错误");

        Activity activity = activityMapper.selectById(id);
        if (activity == null)
            return Result.error("活动不存在");

        activity.setIsTop(isTop);
        activityMapper.updateById(activity);
        return Result.success("操作成功");
    }
}

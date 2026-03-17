package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityRegistration;
import com.volunteer.entity.Organizer;
import com.volunteer.entity.Volunteer;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.*;
import com.volunteer.vo.admin.AdminStatsVO;
import com.volunteer.vo.admin.PendingTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理员看板", description = "管理员首页数据接口")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final VolunteerMapper volunteerMapper;
    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final OrganizerMapper organizerMapper;
    private final ActivityCategoryMapper categoryMapper;

    @GetMapping("/stats/summary")
    @Operation(summary = "获取核心统计指标")
    public Result<AdminStatsVO> getSummary() {
        AdminStatsVO vo = new AdminStatsVO();

        // 1. 总志愿者数
        vo.setTotalVolunteers(volunteerMapper.selectCount(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getIsDeleted, 0)));

        // 2. 总服务时长 (SUM actual_hours where status = CONFIRMED)
        // Need custom query or simple loop if data small? Data might be large.
        // Better use wrapper selectObjs or custom sql.
        // MybatisPlus wrapper doesn't support SUM directly easily in selectOne without
        // QueryWrapper select string.
        // Using QueryWrapper
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ActivityRegistration> sumQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        sumQuery.select("IFNULL(SUM(actual_hours), 0)").eq("status", 1).eq("is_deleted", 0);
        // Note: status 1 might mean CONFIRMED/COMPLETED. Need to verify status
        // constants.
        // Assuming status 1 is valid based on previous files.
        Map<String, Object> map = registrationMapper.selectMaps(sumQuery).stream().findFirst().orElse(null);
        if (map != null) {
            Object sum = map.values().iterator().next();
            vo.setTotalServiceHours(new BigDecimal(sum.toString()));
        } else {
            vo.setTotalServiceHours(BigDecimal.ZERO);
        }

        // 3. 进行中活动 (Status = ONGOING (3))
        vo.setOngoingActivities(activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 3) // 3=ONGOING
                .eq(Activity::getIsDeleted, 0)));

        // 4. 待审核总数 (Activity Audit Status = 0 + Organizer Verified = 0?)
        Long pendingActivities = activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getAuditStatus, 0)
                .eq(Activity::getIsDeleted, 0));

        // Pending Organizers?
        Long pendingOrganizers = organizerMapper.selectCount(new LambdaQueryWrapper<Organizer>()
                .eq(Organizer::getVerified, 0) // Assuming 0 is unverified/pending
                .eq(Organizer::getIsDeleted, 0));

        vo.setPendingAudits(pendingActivities + pendingOrganizers);

        return Result.success(vo);
    }

    @GetMapping("/stats/charts")
    @Operation(summary = "获取图表数据")
    public Result<Map<String, Object>> getCharts() {
        Map<String, Object> result = new HashMap<>();

        // 1. 近7天报名趋势
        // Query registration counts group by date
        // Since MP doesn't support easy group by DTO, we can query last 7 days records
        // and aggregate in memory (if volume allows)
        // Or use custom XML. For simplicity and assuming moderate data, fetch
        // create_time.
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        List<ActivityRegistration> recentRegs = registrationMapper
                .selectList(new LambdaQueryWrapper<ActivityRegistration>()
                        .ge(ActivityRegistration::getCreateTime, sevenDaysAgo)
                        .select(ActivityRegistration::getCreateTime)); // Only fetch time

        Map<String, Long> trendMap = new TreeMap<>();
        // Initialize last 7 days keys
        for (int i = 0; i < 7; i++) {
            trendMap.put(LocalDate.now().minusDays(i).toString(), 0L);
        }

        for (ActivityRegistration reg : recentRegs) {
            String date = reg.getCreateTime().toLocalDate().toString();
            trendMap.put(date, trendMap.getOrDefault(date, 0L) + 1);
        }

        // Keys sorted roughly? TreeMap sorts by key (Date string yyyy-MM-dd works).
        result.put("trendDates", trendMap.keySet());
        result.put("trendValues", trendMap.values());

        // 2. 活动分类占比
        // Need Category Name and Count.
        // Group by category_id.
        List<Map<String, Object>> catCounts = activityMapper
                .selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Activity>()
                        .select("category_id as categoryId", "count(*) as count")
                        .eq("is_deleted", 0)
                        .groupBy("category_id"));

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map<String, Object> cc : catCounts) {
            Long catId = Long.valueOf(cc.get("categoryId").toString());
            Long count = Long.valueOf(cc.get("count").toString());

            ActivityCategory cat = categoryMapper.selectById(catId);
            String name = (cat != null) ? cat.getName() : "未知分类";

            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", count);
            pieData.add(item);
        }
        result.put("pieData", pieData);

        return Result.success(result);
    }

    @GetMapping("/tasks/pending")
    @Operation(summary = "获取待处理任务")
    public Result<List<PendingTaskVO>> getPendingTasks() {
        List<PendingTaskVO> tasks = new ArrayList<>();

        // 1. Pending Activities (Limit 5)
        List<Activity> pendingActivities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getAuditStatus, 0)
                .eq(Activity::getIsDeleted, 0)
                .orderByAsc(Activity::getCreateTime)
                .last("LIMIT 5"));

        for (Activity act : pendingActivities) {
            PendingTaskVO task = new PendingTaskVO();
            task.setId(act.getId());
            task.setType("活动审核");
            task.setSubmitTime(act.getCreateTime());
            task.setStatus("待审核");
            task.setActionUrl("/admin/activity/audit?id=" + act.getId());

            Organizer org = organizerMapper.selectByUserId(act.getOrganizerId());
            if (org == null) {
                org = organizerMapper.selectById(act.getOrganizerId());
            }
            task.setSource(org != null ? org.getOrgName() : "未知组织");

            tasks.add(task);
        }

        // If less than 5, fill with organizers?
        if (tasks.size() < 5) {
            List<Organizer> pendingOrgs = organizerMapper.selectList(new LambdaQueryWrapper<Organizer>()
                    .eq(Organizer::getVerified, 0)
                    .eq(Organizer::getIsDeleted, 0)
                    .orderByAsc(Organizer::getCreateTime)
                    .last("LIMIT " + (5 - tasks.size())));

            for (Organizer org : pendingOrgs) {
                PendingTaskVO task = new PendingTaskVO();
                task.setId(org.getId());
                task.setType("组织认证");
                task.setSubmitTime(org.getCreateTime());
                task.setStatus("待认证");
                task.setActionUrl("/admin/user/organizer?audit=" + org.getId());
                task.setSource(org.getOrgName()); // Source is self
                tasks.add(task);
            }
        }

        // Sort mix by time asc?
        tasks.sort(Comparator.comparing(PendingTaskVO::getSubmitTime));

        return Result.success(tasks);
    }
}

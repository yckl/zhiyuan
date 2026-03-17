package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.dto.ActivityDTO;
import com.volunteer.dto.ActivityPageQuery;
import com.volunteer.dto.CheckinRequest;
import com.volunteer.dto.ActivityRequest;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Organizer;
import com.volunteer.mapper.OrganizerMapper;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "活动的增删改查")
public class ActivityController {

    private final ActivityService activityService;
    private final OrganizerMapper organizerMapper;

    /**
     * 发布活动（仅组织者）
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "发布活动", description = "创建新的志愿活动（仅组织者和管理员）")
    public Result<Activity> createActivity(@Valid @RequestBody ActivityRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Activity activity = activityService.createActivity(request, userId);
            return Result.success("活动创建成功，待审核", activity);
        } catch (Exception e) {
            log.error("创建活动失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改活动（仅组织者）
     */
    @PutMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "修改活动", description = "修改活动信息（仅发布者和管理员）")
    public Result<Void> updateActivity(@Valid @RequestBody ActivityRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            activityService.updateActivity(request, userId);
            return Result.success("修改成功", null);
        } catch (Exception e) {
            log.error("修改活动失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情", description = "根据ID获取活动详细信息（含组织者完整信息）")
    public Result<Object> getActivityDetail(@PathVariable Long id) {
        try {
            // 使用新的VO方法获取完整详情（包含组织者信息）
            var activity = ((com.volunteer.service.impl.ActivityServiceImpl) activityService).getActivityDetailVO(id);
            // 增加浏览次数
            activityService.incrementViewCount(id);

            // 记录浏览足迹
            try {
                Long userId = SecurityUtils.getUserId();
                if (userId != null) {
                    activityService.recordView(userId, id);
                }
            } catch (Exception e) {
                log.warn("记录浏览足迹失败: {}", e.getMessage());
            }
            return Result.success(activity);
        } catch (Exception e) {
            log.error("获取活动详情失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动已报名参与者（公开信息）
     */
    @GetMapping("/{id}/participants")
    @Operation(summary = "获取活动参与者", description = "获取活动已通过审核的参与者列表")
    public Result<List<Map<String, Object>>> getActivityParticipants(@PathVariable Long id) {
        try {
            return Result.success(activityService.getActivityParticipants(id));
        } catch (Exception e) {
            log.error("获取活动参与者失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询活动列表
     */
    @GetMapping("/list")
    @Operation(summary = "活动列表", description = "分页查询活动列表，支持按标题、分类、状态筛选")
    public Result<Page<ActivityDTO>> listActivities(ActivityPageQuery query) {
        try {
            Page<ActivityDTO> page = activityService.pageActivities(query);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询活动列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议", description = "根据关键词获取活动建议")
    public Result<java.util.List<com.volunteer.dto.ActivityDTO>> getSuggestions(@RequestParam String keyword) {
        try {
            return Result.success(activityService.getSearchSuggestions(keyword));
        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage());
            return Result.success(java.util.Collections.emptyList());
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "删除活动", description = "删除活动（仅发布者和管理员）")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            activityService.deleteActivity(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除活动失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核活动（仅管理员）
     */
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核活动", description = "审核活动（仅管理员）")
    public Result<Void> auditActivity(
            @PathVariable Long id,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditRemark) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            activityService.auditActivity(id, auditStatus, auditRemark, userId);
            return Result.success("审核完成", null);
        } catch (Exception e) {
            log.error("审核活动失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我发布的活动（组织者）
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @Operation(summary = "我的活动", description = "获取当前组织者发布的活动")
    public Result<Page<ActivityDTO>> getMyActivities(ActivityPageQuery query) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            query.setOrganizerId(userId);
            Page<ActivityDTO> page = activityService.pageActivities(query);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询我的活动失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 扫码签到
     */
    @PostMapping("/checkin")
    @Operation(summary = "扫码签到", description = "志愿者通过扫描活动二维码进行签到")
    public Result<String> checkin(@RequestBody CheckinRequest request) {
        Long activityId = request.getActivityId();
        if (activityId == null) {
            return Result.error("活动ID不能为空");
        }

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            activityService.checkin(activityId, userId);
            return Result.success("签到成功，积分+10", null);
        } catch (Exception e) {
            log.error("签到失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有组织者列表（用于筛选）
     */
    @GetMapping("/organizers")
    @Operation(summary = "获取组织者列表", description = "获取所有组织者/主办方列表，用于活动筛选")
    public Result<List<Map<String, Object>>> getOrganizers() {
        try {
            List<Organizer> organizers = organizerMapper.selectList(null);
            List<Map<String, Object>> result = organizers.stream()
                    .map(org -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", org.getUserId()); // 使用userId作为organizerId进行筛选
                        map.put("orgName", org.getOrgName());
                        map.put("orgType", org.getOrgType());
                        map.put("logo", org.getLogo());
                        return map;
                    })
                    .collect(Collectors.toList());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取组织者列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

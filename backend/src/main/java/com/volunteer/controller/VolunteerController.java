package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.VolunteerDTO;
import com.volunteer.dto.VolunteerUpdateRequest;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.VolunteerService;
import com.volunteer.vo.VolunteerStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 志愿者个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/volunteer")
@RequiredArgsConstructor
@Tag(name = "志愿者个人中心", description = "志愿者个人信息管理")
public class VolunteerController {

    private final VolunteerService volunteerService;

    /**
     * 获取当前登录志愿者的详细信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录志愿者的详细信息")
    public Result<VolunteerDTO> getProfile() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            VolunteerDTO profile = volunteerService.getVolunteerProfile(userId);
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取个人信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "修改个人信息", description = "修改当前登录志愿者的个人信息")
    public Result<Void> updateProfile(@RequestBody VolunteerUpdateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            volunteerService.updateProfile(userId, request);
            return Result.success("修改成功", null);
        } catch (Exception e) {
            log.error("修改个人信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取积分信息
     */
    @GetMapping("/points")
    @Operation(summary = "获取积分信息", description = "获取当前志愿者的积分信息")
    public Result<VolunteerDTO> getPointsInfo() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            VolunteerDTO profile = volunteerService.getVolunteerProfile(userId);
            // 只返回积分相关信息
            VolunteerDTO pointsInfo = new VolunteerDTO();
            pointsInfo.setTotalPoints(profile.getTotalPoints());
            pointsInfo.setAvailablePoints(profile.getAvailablePoints());
            pointsInfo.setTotalHours(profile.getTotalHours());
            pointsInfo.setLevel(profile.getLevel());
            return Result.success(pointsInfo);
        } catch (Exception e) {
            log.error("获取积分信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取个人统计详情（雷达图、热力图等）
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计详情", description = "获取志愿者雷达图、热力图、荣誉证书等")
    public Result<VolunteerStatsVO> getPersonalStats() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            VolunteerStatsVO stats = volunteerService.getPersonalStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取个人统计失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取个人中心统计简报 (实时同步)
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取个人中心统计数据", description = "获取志愿时长、服务次数、累计积分等实时统计")
    public Result<VolunteerDTO> getStatistics() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            // getVolunteerProfile 已经更新为实时计算
            VolunteerDTO stats = volunteerService.getVolunteerProfile(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

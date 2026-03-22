package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Organizer;
import com.volunteer.service.OrganizerService;
import com.volunteer.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 组织者通用管理控制器
 */
@RestController
@RequestMapping("/organizer")
@RequiredArgsConstructor
@Tag(name = "组织者管理", description = "组织者个人信息管理")
@PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
public class OrganizerController {

    private final OrganizerService organizerService;

    /**
     * 获取组织者档案
     */
    @GetMapping("/profile")
    @Operation(summary = "获取组织档案", description = "获取当前组织者的详细信息")
    public Result<Map<String, Object>> getProfile() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Organizer organizer = organizerService.getByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", organizer.getId());
        data.put("name", organizer.getOrgName());
        data.put("logo", organizer.getLogo());
        data.put("description", organizer.getDescription());
        data.put("address", organizer.getAddress());
        data.put("contactPerson", organizer.getContactPerson());
        data.put("contactPhone", organizer.getContactPhone());
        data.put("contactEmail", organizer.getContactEmail());
        data.put("verified", organizer.getVerified());
        // 暂不支持自动审核字段，返回默认false
        data.put("autoAudit", false);

        return Result.success(data);
    }

    /**
     * 更新组织者档案
     */
    @PutMapping("/profile")
    @Operation(summary = "更新组织档案", description = "更新组织基本信息")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Organizer organizer = organizerService.getByUserId(userId);

        if (params.containsKey("description")) {
            organizer.setDescription((String) params.get("description"));
        }
        if (params.containsKey("address")) {
            organizer.setAddress((String) params.get("address"));
        }
        if (params.containsKey("contactPerson")) {
            organizer.setContactPerson((String) params.get("contactPerson"));
        }
        if (params.containsKey("contactPhone")) {
            organizer.setContactPhone((String) params.get("contactPhone"));
        }
        if (params.containsKey("logo")) {
            organizer.setLogo((String) params.get("logo"));
        }
        // orgName 通常不允许随意修改，需要管理员审核，这里暂不更新

        organizerService.updateById(organizer);

        return Result.success("更新成功", null);
    }
}

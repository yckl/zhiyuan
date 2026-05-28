package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.dto.UserPageQuery;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.volunteer.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 仅限管理员访问
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "管理员用户管理接口")
public class UserController {

    private final SysUserMapper sysUserMapper;
    private final VolunteerMapper volunteerMapper;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "获取系统所有用户列表，支持按用户名、角色、状态筛选")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserVO>> listUsers(UserPageQuery query) {
        log.info("查询用户列表: {}", query);

        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名模糊匹配）
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysUser::getUsername, query.getKeyword());
        }

        // 角色筛选
        if (StringUtils.hasText(query.getRole())) {
            wrapper.eq(SysUser::getRole, query.getRole());
        }

        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }

        // 排序
        if ("asc".equalsIgnoreCase(query.getOrderDirection())) {
            wrapper.orderByAsc(SysUser::getCreateTime);
        } else {
            wrapper.orderByDesc(SysUser::getCreateTime);
        }

        // 分页查询
        Page<SysUser> page = new Page<>(query.getPage(), query.getSize());
        Page<SysUser> sysUserPage = sysUserMapper.selectPage(page, wrapper);

        // 获取所有志愿者的userId用于关联真实姓名
        List<Long> userIds = sysUserPage.getRecords().stream()
                .filter(u -> SysUser.ROLE_VOLUNTEER.equals(u.getRole()))
                .map(SysUser::getId)
                .collect(Collectors.toList());

        Map<Long, String> realNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<Volunteer> volunteerWrapper = new LambdaQueryWrapper<>();
            volunteerWrapper.in(Volunteer::getUserId, userIds);
            List<Volunteer> volunteers = volunteerMapper.selectList(volunteerWrapper);
            volunteers.forEach(v -> realNameMap.put(v.getUserId(), v.getName()));
        }

        // 转换为VO并构建返回结果
        List<UserVO> voList = sysUserPage.getRecords().stream()
                .map(user -> convertToVO(user, realNameMap))
                .collect(Collectors.toList());

        // 构建返回的Page对象
        Page<UserVO> resultPage = new Page<>(query.getPage(), query.getSize(), sysUserPage.getTotal());
        resultPage.setRecords(voList);

        return Result.success(resultPage);
    }

    /**
     * 修改用户状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态", description = "禁用或启用用户账号")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("修改用户状态: id={}, status={}", id, status);

        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. 不能禁用自己
        Long currentUserId = SecurityUtils.getUserId();
        if (id.equals(currentUserId)) {
            return Result.error("不能禁用自己的账号");
        }

        // 3. 不能禁用管理员
        if (SysUser.ROLE_ADMIN.equals(user.getRole())) {
            return Result.error("不能禁用管理员账号");
        }

        // 4. 更新状态
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, id)
                .set(SysUser::getStatus, status)
                .set(SysUser::getUpdateTime, LocalDateTime.now());
        sysUserMapper.update(null, updateWrapper);

        log.info("用户 {} 状态已更新为 {}", user.getUsername(), status);
        return Result.success("用户已" + (status == 1 ? "启用" : "禁用"), null);
    }

    /**
     * 更新当前用户个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "用户更新自己的头像、邮箱、手机号")
    public Result<Void> updateMyProfile(@RequestBody SysUser user) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return Result.unauthorized("请先登录");
        }

        log.info("用户 {} 更新个人信息", currentUserId);

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, currentUserId);

        boolean needUpdate = false;
        if (StringUtils.hasText(user.getAvatar())) {
            updateWrapper.set(SysUser::getAvatar, user.getAvatar());
            needUpdate = true;
        }
        if (StringUtils.hasText(user.getEmail())) {
            updateWrapper.set(SysUser::getEmail, user.getEmail());
            needUpdate = true;
        }
        if (StringUtils.hasText(user.getPhone())) {
            updateWrapper.set(SysUser::getPhone, user.getPhone());
            needUpdate = true;
        }

        if (needUpdate) {
            updateWrapper.set(SysUser::getUpdateTime, LocalDateTime.now());
            sysUserMapper.update(null, updateWrapper);
            return Result.success("更新成功", null);
        }

        return Result.error("未发现需要更新的内容");
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计", description = "获取各类用户的数量统计")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        Long total = sysUserMapper.selectCount(null);
        stats.put("total", total);

        // 志愿者数量
        LambdaQueryWrapper<SysUser> volunteerWrapper = new LambdaQueryWrapper<>();
        volunteerWrapper.eq(SysUser::getRole, SysUser.ROLE_VOLUNTEER);
        Long volunteers = sysUserMapper.selectCount(volunteerWrapper);
        stats.put("volunteers", volunteers);

        // 组织者数量
        LambdaQueryWrapper<SysUser> organizerWrapper = new LambdaQueryWrapper<>();
        organizerWrapper.eq(SysUser::getRole, SysUser.ROLE_ORGANIZER);
        Long organizers = sysUserMapper.selectCount(organizerWrapper);
        stats.put("organizers", organizers);

        // 禁用用户数量
        LambdaQueryWrapper<SysUser> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(SysUser::getStatus, SysUser.STATUS_DISABLED);
        Long disabled = sysUserMapper.selectCount(disabledWrapper);
        stats.put("disabled", disabled);

        return Result.success(stats);
    }

    /**
     * 将 SysUser 转换为 UserVO
     */
    @SuppressWarnings("null")
    private UserVO convertToVO(SysUser user, Map<Long, String> realNameMap) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 设置真实姓名（从志愿者表获取）
        if (SysUser.ROLE_VOLUNTEER.equals(user.getRole())) {
            vo.setRealName(realNameMap.get(user.getId()));
        } else if (SysUser.ROLE_ORGANIZER.equals(user.getRole())) {
            // 组织者使用用户名作为名称
            vo.setRealName(user.getUsername());
        } else {
            vo.setRealName("管理员");
        }

        return vo;
    }
}

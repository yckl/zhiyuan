package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.volunteer.common.Result;
import com.volunteer.dto.VolunteerRegisterRequest;
import com.volunteer.entity.PointsRecord;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.PointsRecordMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/volunteer")
@RequiredArgsConstructor
@Tag(name = "志愿者管理", description = "管理员志愿者增删及积分调整")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVolunteerController {

    private final SysUserMapper sysUserMapper;
    private final VolunteerMapper volunteerMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final SysMessageService sysMessageService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 积分调整
     */
    @PostMapping("/points/adjust")
    @Operation(summary = "手动调整积分", description = "管理员手动调整志愿者积分")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adjustPoints(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer points = Integer.valueOf(body.get("points").toString());
        String reason = (String) body.get("reason");

        if (points == 0) {
            return Result.error("积分变动不能为0");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("调整原因不能为空");
        }

        // 1. 获取志愿者信息
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getUserId, userId));

        if (volunteer == null) {
            return Result.error("未找到对应的志愿者信息");
        }

        // 2. 检查余额 (如果是扣减)
        if (points < 0 && (volunteer.getAvailablePoints() + points < 0)) {
            return Result.error("志愿者可用积分不足，无法扣减");
        }

        // 3. 更新积分
        // 只有增加积分时才计入 totalHours/totalPoints? 此处通常totalPoints只增不减，但手动调整可能也需要减?
        // 假设totalPoints是累计获得，不随消费减少，但如果是管理员惩罚扣分，是否减少累计?
        // 按照常规逻辑：消费不减累计，惩罚需要视业务而定。这里简单处理：available变动，total只增不减(除非是负数调整修正错误?)
        // 策略：如果 points > 0，则 totalPoints += points。如果 points < 0，totalPoints 不变
        // (视为消费/惩罚，不影响"历史总获得")。
        // 或者管理员调整为了修正错误数据，那么total也要动。这里采取更通用的：total += points (允许减少)

        volunteer.setTotalPoints(volunteer.getTotalPoints() + points);
        volunteer.setAvailablePoints(volunteer.getAvailablePoints() + points);
        volunteerMapper.updateById(volunteer);

        // 4. 记录流水
        PointsRecord record = new PointsRecord();
        record.setVolunteerId(volunteer.getId()); // 注意是 volunteer_id (pk) 不是 user_id
        record.setPoints(points);
        record.setBalance(volunteer.getAvailablePoints());
        record.setType(PointsRecord.Type.ADMIN);
        record.setDescription("【管理员调整】" + reason);
        record.setBizId(0L); // 0 表示无特定业务ID
        pointsRecordMapper.insert(record);

        // 5. 发送通知
        String title = points > 0 ? "积分到账通知" : "积分扣除通知";
        String content = String.format("管理员%s了您 %d 积分。原因：%s",
                points > 0 ? "奖励" : "扣除",
                Math.abs(points),
                reason);
        /*
         * Assume sysMessageService.sendMessage(receiverId, senderId, title, content,
         * type)
         * senderId = 1L (Admin)
         * type = "SYSTEM" (as string? let's check interface)
         */
        // Based on previous reads, type might be String or Integer. SysMessageService
        // interface says:
        // void sendMessage(Long receiverId, Long senderId, String title, String
        // content, String type);
        sysMessageService.sendMessage(userId, 1L, title, content, "SYSTEM");

        return Result.success("积分调整成功");
    }

    /**
     * 新增志愿者
     */
    @PostMapping("/add")
    @Operation(summary = "新增志愿者", description = "管理员直接添加志愿者账号")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addVolunteer(@RequestBody VolunteerRegisterRequest request) {
        // 1. 检查用户名是否存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        // 2. 创建 SysUser
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(SysUser.ROLE_VOLUNTEER);
        user.setStatus(SysUser.STATUS_NORMAL);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAvatar("https://api.dicebear.com/7.x/notionists/svg?seed=" + request.getUsername());
        sysUserMapper.insert(user);

        // 3. 创建 Volunteer Profile
        Volunteer volunteer = new Volunteer();
        volunteer.setUserId(user.getId());
        volunteer.setName(request.getName());
        volunteer.setGender(request.getGender() != null ? request.getGender() : 0);
        volunteer.setStudentNo(request.getStudentNo() != null ? request.getStudentNo() : request.getUsername());
        // volunteer.setPhone(request.getPhone()); // field does not exist in Volunteer
        // entity
        volunteer.setCollege(request.getCollege());
        volunteer.setMajor(request.getMajor());
        volunteer.setClassName(request.getClassName());
        volunteer.setGrade(request.getGrade());
        // Init points
        volunteer.setTotalPoints(0);
        volunteer.setAvailablePoints(0);
        volunteer.setTotalHours(java.math.BigDecimal.ZERO);
        volunteerMapper.insert(volunteer);

        return Result.success("志愿者添加成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除用户", description = "删除用户及其关联信息")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteUser(@PathVariable Long id) {
        // id is SysUser id
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 1. 逻辑删除 SysUser
        sysUserMapper.deleteById(id);

        // 2. 逻辑删除关联角色信息
        if (SysUser.ROLE_VOLUNTEER.equals(user.getRole())) {
            volunteerMapper.delete(new LambdaUpdateWrapper<Volunteer>()
                    .eq(Volunteer::getUserId, id));
        }
        // If organizer, maybe delete organizer? (Out of scope but good practice)

        return Result.success("删除成功");
    }
}

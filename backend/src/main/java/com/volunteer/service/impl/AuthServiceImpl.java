package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.LoginRequest;
import com.volunteer.dto.LoginResponse;
import com.volunteer.dto.VolunteerRegisterRequest;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.security.JwtUtils;
import com.volunteer.security.LoginUser;
import com.volunteer.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.volunteer.exception.BusinessException;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper sysUserMapper;
    private final VolunteerMapper volunteerMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            // 获取认证后的用户信息
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysUser sysUser = loginUser.getSysUser();

            // 生成JWT Token
            String token = jwtUtils.generateToken(
                    sysUser.getId(),
                    sysUser.getUsername(),
                    sysUser.getRole());

            // 更新最后登录时间
            SysUser updateUser = new SysUser();
            updateUser.setId(sysUser.getId());
            updateUser.setLastLoginTime(LocalDateTime.now());
            sysUserMapper.updateById(updateUser);

            log.info("用户登录成功: {}", request.getUsername());

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(sysUser.getId())
                    .username(sysUser.getUsername())
                    .role(sysUser.getRole())
                    .avatar(sysUser.getAvatar())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("登录失败，用户名或密码错误: {}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser registerVolunteer(VolunteerRegisterRequest request) {
        log.info("志愿者注册: {}", request.getUsername());

        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, request.getUsername());
        if (sysUserMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建系统用户
        SysUser sysUser = new SysUser();
        sysUser.setUsername(request.getUsername());
        sysUser.setPassword(passwordEncoder.encode(request.getPassword()));
        sysUser.setRole(SysUser.ROLE_VOLUNTEER);
        sysUser.setStatus(SysUser.STATUS_NORMAL);
        sysUser.setPhone(request.getPhone());
        sysUser.setEmail(request.getEmail());
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUser.setIsDeleted(0);

        sysUserMapper.insert(sysUser);
        log.info("创建系统用户成功, ID: {}", sysUser.getId());

        // 创建志愿者信息
        Volunteer volunteer = new Volunteer();
        volunteer.setUserId(sysUser.getId());
        volunteer.setName(request.getName());
        volunteer.setGender(request.getGender() != null ? request.getGender() : Volunteer.GENDER_UNKNOWN);
        volunteer.setStudentNo(request.getStudentNo());
        volunteer.setCollege(request.getCollege());
        volunteer.setMajor(request.getMajor());
        volunteer.setClassName(request.getClassName());
        volunteer.setGrade(request.getGrade());
        volunteer.setTotalHours(BigDecimal.ZERO);
        volunteer.setTotalPoints(0);
        volunteer.setAvailablePoints(0);
        volunteer.setLevel(1);
        volunteer.setCreateTime(LocalDateTime.now());
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteer.setIsDeleted(0);

        volunteerMapper.insert(volunteer);
        log.info("创建志愿者信息成功, ID: {}", volunteer.getId());

        return sysUser;
    }

    @Override
    public SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getSysUser();
        }
        return null;
    }

    @Override
    public SysUser getUserById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    @Override
    public void updateUserAvatar(String username, String avatarUrl) {
        SysUser currentUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (currentUser != null) {
            currentUser.setAvatar(avatarUrl);
            sysUserMapper.updateById(currentUser);
            log.info("用户 {} 头像已更新: {}", username, avatarUrl);
        }
    }
}

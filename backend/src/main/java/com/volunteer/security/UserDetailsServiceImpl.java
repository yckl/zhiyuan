package com.volunteer.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.entity.SysUser;
import com.volunteer.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义UserDetailsService实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);

        // 根据用户名查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        if (sysUser == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        if (SysUser.STATUS_DISABLED.equals(sysUser.getStatus())) {
            log.warn("用户已禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用");
        }

        if (SysUser.STATUS_LOCKED.equals(sysUser.getStatus())) {
            log.warn("用户已锁定: {}", username);
            throw new UsernameNotFoundException("用户已被锁定");
        }

        return new LoginUser(sysUser);
    }
}

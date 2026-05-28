package com.volunteer.security;

import com.volunteer.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义UserDetails实现
 */
@Getter
public class LoginUser implements UserDetails {

    private final SysUser sysUser;

    public LoginUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + sysUser.getRole()));
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !SysUser.STATUS_LOCKED.equals(sysUser.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return SysUser.STATUS_NORMAL.equals(sysUser.getStatus());
    }

    public Long getUserId() {
        return sysUser.getId();
    }

    public String getRole() {
        return sysUser.getRole();
    }
}

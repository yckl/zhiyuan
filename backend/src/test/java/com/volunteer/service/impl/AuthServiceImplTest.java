package com.volunteer.service.impl;

import com.volunteer.dto.LoginRequest;
import com.volunteer.dto.LoginResponse;
import com.volunteer.entity.SysUser;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.security.JwtUtils;
import com.volunteer.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private VolunteerMapper volunteerMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser mockUser;
    private LoginUser loginUser;
    
    @BeforeEach
    void setUp() {
        mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encoded_password");
        mockUser.setRole(SysUser.ROLE_VOLUNTEER);
        mockUser.setAvatar("http://example.com/avatar.jpg");
        
        loginUser = new LoginUser(mockUser);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(1L, "testuser", SysUser.ROLE_VOLUNTEER)).thenReturn("mocked_jwt_token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals(SysUser.ROLE_VOLUNTEER, response.getRole());
        
        // Verify mapper was called to update last login time
        verify(sysUserMapper, times(1)).updateById(any(SysUser.class));
    }

    @Test
    void testLogin_Failure_BadCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(jwtUtils, never()).generateToken(any(), any(), any());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }
}

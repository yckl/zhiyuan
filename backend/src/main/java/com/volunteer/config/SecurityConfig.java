package com.volunteer.config;

import com.volunteer.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        /**
         * 密码编码器
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * 认证管理器
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * 安全过滤链配置
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 禁用CSRF（前后端分离不需要）
                                .csrf(AbstractHttpConfigurer::disable)
                                // 启用CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // 禁用Session（使用JWT）
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // 配置请求授权
                                .authorizeHttpRequests(auth -> auth
                                                // 放行所有OPTIONS请求（CORS预检的关键）
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                // 放行认证相关接口
                                                .requestMatchers("/auth/**").permitAll()
                                                // 放行Swagger文档
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/swagger-resources/**",
                                                                "/v3/api-docs/**",
                                                                "/doc.html",
                                                                "/webjars/**")
                                                .permitAll()
                                                // 放行静态资源和文件访问
                                                .requestMatchers("/static/**", "/favicon.ico", "/files/**").permitAll()
                                                // 放行健康检查
                                                .requestMatchers("/actuator/**").permitAll()
                                                // 放行公开接口（首页统计、分类列表、活动列表、公告列表）
                                                .requestMatchers("/statistics/index", "/category/list",
                                                                "/activity/list", "/activity/{id}",
                                                                "/activity/{id}/participants",
                                                                "/notice/list", "/notice/{id}", "/banner/list")
                                                .permitAll()
                                                // 放行系统公开接口
                                                .requestMatchers("/system/config").permitAll()
                                                // 放行评论公开接口
                                                .requestMatchers("/comment/list", "/comment/count", "/comment/rating")
                                                .permitAll()
                                                // 放行推荐公开接口
                                                .requestMatchers("/recommendation/**")
                                                .permitAll()
                                                // 放行心得公开接口
                                                .requestMatchers("/experience/public/list", "/experience/{id}",
                                                                "/experience/list")
                                                .permitAll()
                                                // 放行课程公开接口
                                                .requestMatchers("/course/list", "/course/{id}")
                                                .permitAll()
                                                // 放行商城公开接口
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/mall/goods/**")
                                                .permitAll()
                                                // 放行状态检查接口（内部会处理未登录情况）
                                                .requestMatchers("/collection/check", "/registration/check/**",
                                                                "/experience/{id}/like/check")
                                                .permitAll()
                                                // 其他请求需要认证
                                                .anyRequest().authenticated())
                                // 添加JWT过滤器
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * CORS配置源
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // 允许的源 - 使用模式匹配支持所有端口和IP
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                // 允许的方法
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                // 允许的头
                configuration.setAllowedHeaders(List.of("*"));
                // 允许携带凭证
                configuration.setAllowCredentials(true);
                // 暴露的头
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
                // 预检请求缓存时间
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * 注册CORS过滤器，优先级最高
         */
        @Bean
        public org.springframework.web.filter.CorsFilter corsFilter() {
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);
                source.registerCorsConfiguration("/**", configuration);
                return new org.springframework.web.filter.CorsFilter(source);
        }
}

package com.volunteer.config;

import com.volunteer.interceptor.MaintenanceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置 - 静态资源映射 & 拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @NonNull
    private final MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 将 /files/** 映射到本地 uploads 目录 (使用绝对路径以提高可靠性)
        String absolutePath = new java.io.File(uploadDir).getAbsolutePath();
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
            absolutePath += java.io.File.separator;
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath);
    }

    @Override
    public void addCorsMappings(@NonNull org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 维护模式拦截器 - 应用于所有路径
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/files/**", "/error"); // 静态资源和错误页除外
    }
}

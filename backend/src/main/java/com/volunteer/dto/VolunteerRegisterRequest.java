package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 志愿者注册请求DTO
 */
@Data
@Schema(description = "志愿者注册请求")
public class VolunteerRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 30, message = "用户名长度为4-30个字符")
    @Schema(description = "用户名（学号）", example = "2024001001")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String name;

    @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
    private Integer gender;

    @Schema(description = "学号", example = "2024001001")
    private String studentNo;

    @Schema(description = "学院", example = "计算机学院")
    private String college;

    @Schema(description = "专业", example = "软件工程")
    private String major;

    @Schema(description = "班级", example = "软件2401")
    private String className;

    @Schema(description = "年级", example = "2024")
    private String grade;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
}

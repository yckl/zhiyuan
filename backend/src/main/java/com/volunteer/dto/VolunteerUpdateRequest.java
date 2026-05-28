package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 志愿者信息更新请求
 */
@Data
@Schema(description = "志愿者信息更新请求")
public class VolunteerUpdateRequest {

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "班级")
    private String className;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "兴趣标签(JSON)")
    private String interestTags;

    @Schema(description = "技能特长")
    private String skills;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系电话")
    private String emergencyPhone;
}

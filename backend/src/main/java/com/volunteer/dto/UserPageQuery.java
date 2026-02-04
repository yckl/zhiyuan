package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户分页查询参数
 */
@Data
@Schema(description = "用户分页查询参数")
public class UserPageQuery {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "用户名/姓名（模糊查询）")
    private String keyword;

    @Schema(description = "角色类型: ADMIN/VOLUNTEER/ORGANIZER")
    private String role;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;

    @Schema(description = "排序字段", example = "createTime")
    private String orderBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String orderDirection = "desc";
}

package com.volunteer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动分页查询参数
 */
@Data
@Schema(description = "活动分页查询参数")
public class ActivityPageQuery {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "活动标题（模糊查询）")
    private String title;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "组织者ID")
    private Long organizerId;

    @Schema(description = "是否精选")
    private Integer isFeatured;

    @Schema(description = "排序字段", example = "createTime")
    private String orderBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String orderDirection = "desc";
}

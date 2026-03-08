package com.volunteer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报名统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动报名统计数据")
public class RegistrationStatsVO {

    @Schema(description = "招募总人数")
    private Integer total;

    @Schema(description = "已通过人数")
    private Long passed;

    @Schema(description = "待审核人数")
    private Long pending;

    @Schema(description = "已拒绝人数")
    private Long rejected;
}

package com.volunteer.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrainingStatsVO {
    private BigDecimal totalHours;
    private Integer completedCount;
    private Integer certificateCount;
    private Integer averageScore;
}

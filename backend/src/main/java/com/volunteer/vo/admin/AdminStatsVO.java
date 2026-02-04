package com.volunteer.vo.admin;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AdminStatsVO implements Serializable {
    private Long totalVolunteers;
    private BigDecimal totalServiceHours;
    private Long ongoingActivities;
    private Long pendingAudits;
}

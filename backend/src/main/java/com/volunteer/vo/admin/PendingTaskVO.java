package com.volunteer.vo.admin;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PendingTaskVO implements Serializable {
    private Long id;
    private String type; // e.g. "活动审核", "组织认证"
    private String source; // Organizer Name
    private LocalDateTime submitTime;
    private String status; // "待审核"
    private String actionUrl; // Frontend route
}

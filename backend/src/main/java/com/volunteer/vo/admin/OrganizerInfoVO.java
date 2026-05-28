package com.volunteer.vo.admin;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrganizerInfoVO implements Serializable {
    private Long id; // Organizer ID
    private Long userId;
    private String orgName;
    private String orgType;
    private String contactPerson;
    private String contactPhone;
    private String logo;
    private Integer verified; // 0-Pending, 1-Verified, 2-Rejected
    private Integer status; // SysUser status: 0-Disabled, 1-Normal
    private Long activityCount;
    private LocalDateTime createTime;
    private String auditReason; // For rejection
}

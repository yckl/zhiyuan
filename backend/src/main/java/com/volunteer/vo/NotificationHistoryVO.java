package com.volunteer.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationHistoryVO implements Serializable {
    private String title;
    private String content;
    private LocalDateTime createTime;
    private Long totalCount;
    private Long readCount;
    // Optional: Derived from title or logic, but for now just placeholder
    private String activityName;
    private String targetGroup;
}

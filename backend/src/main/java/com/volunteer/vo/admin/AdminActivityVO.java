package com.volunteer.vo.admin;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminActivityVO implements Serializable {
    private Long id;
    private String title;
    private String coverImage;
    private Long organizerId;
    private String organizerName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private Integer status; // Activity Status
    private Integer isTop;
    private Integer isFeatured;
    private String auditRemark; // For offline reason
}

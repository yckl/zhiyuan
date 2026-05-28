package com.volunteer.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerAvatar;
    private Integer score;
    private String content;
    private String replyContent;
    private LocalDateTime replyTime;
    private Integer status;
    private Boolean isAnonymous;
    private LocalDateTime createTime;
}

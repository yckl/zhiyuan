package com.volunteer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendationDTO {
    private Long id;
    private String title;
    private String categoryName;
    private String coverImage;
    private Integer score;
    private String algoTag;
    private String reason;
    private BigDecimal serviceHours;
    private Integer pointsReward;
    private String startTime;
}

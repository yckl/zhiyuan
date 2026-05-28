package com.volunteer.vo;

import lombok.Data;

@Data
public class ReviewStatsVO {
    /** 平均分 */
    private Double averageScore;
    /** 总评价数 */
    private Long totalReviews;
    /** 好评率 (4-5星占比) */
    private Double positiveRate;
    /** 差评数 (1-2星数量) */
    private Long negativeCount;
    /** 待回复数 */
    private Long pendingReplyCount;
}

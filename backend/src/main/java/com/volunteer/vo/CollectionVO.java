package com.volunteer.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏详情 VO
 * 用于返回收藏列表时包含关联内容的详细信息
 */
@Data
public class CollectionVO {

    /** 收藏ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标类型 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 收藏时间 */
    private LocalDateTime createTime;

    // ========== 关联内容通用字段 ==========

    /** 标题（活动/课程/公告标题） */
    private String title;

    /** 封面图片 */
    private String coverImage;

    /** 摘要/简介 */
    private String summary;

    /** 状态 */
    private Integer status;

    // ========== 活动特有字段 ==========

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 活动地点 */
    private String location;

    // ========== 课程特有字段 ==========

    /** 讲师 */
    private String instructor;

    /** 时长(分钟) */
    private Integer duration;

    // ========== 公告特有字段 ==========

    /** 发布时间 */
    private LocalDateTime publishTime;

    // ========== 心得特有字段 ==========

    /** 图片列表(JSON) */
    private String images;
}

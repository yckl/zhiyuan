package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体类
 * 对应表: activity
 */
@Data
@Accessors(chain = true)
@TableName("activity")
public class Activity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** 分类ID */
    private Long categoryId;

    /** 发布组织者ID */
    private Long organizerId;

    /** 封面图片 */
    private String coverImage;

    /** 活动图片列表(JSON) */
    private String images;

    /** 活动摘要 */
    private String summary;

    /** 活动详情（富文本） */
    private String content;

    /** 活动地点 */
    private String location;

    /** 纬度 */
    private BigDecimal latitude;

    /** 经度 */
    private BigDecimal longitude;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 报名开始时间 */
    private LocalDateTime registerStart;

    /** 报名截止时间 */
    private LocalDateTime registerEnd;

    /** 活动截止时间（报名的最后期限） */
    private LocalDateTime deadline;

    /** 最大参与人数（0-不限） */
    private Integer maxParticipants;

    /** 当前报名人数 */
    private Integer currentParticipants;

    /** 最小开展人数 */
    private Integer minParticipants;

    /** 志愿时长（小时） */
    private BigDecimal serviceHours;

    /** 积分奖励 */
    private Integer pointsReward;

    /** 参与要求 */
    private String requirements;

    /** 联系方式 */
    private String contactInfo;

    /** 状态：0-草稿，1-待审核，2-已发布，3-进行中，4-已结束，5-已取消 */
    private Integer status;

    /** 审核状态：0-待审核，1-通过，2-拒绝 */
    private Integer auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditorId;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 是否精选推荐 */
    private Integer isFeatured;

    /** 是否置顶 */
    private Integer isTop;

    /** 活动标签(JSON) */
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    /** 签到码（6位数字） */
    private String checkinCode;

    // ========== 状态常量 ==========
    public static final Integer STATUS_DRAFT = 0;
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_PUBLISHED = 2;
    public static final Integer STATUS_ONGOING = 3;
    public static final Integer STATUS_ENDED = 4;
    public static final Integer STATUS_CANCELLED = 5;

    // ========== 审核状态常量 ==========
    public static final Integer AUDIT_PENDING = 0;
    public static final Integer AUDIT_APPROVED = 1;
    public static final Integer AUDIT_REJECTED = 2;
}

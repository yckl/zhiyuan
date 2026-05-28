package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 志愿心得实体类
 * 对应表: experience
 */
@Data
@Accessors(chain = true)
@TableName("experience")
public class Experience implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 关联活动ID */
    private Long activityId;

    /** 心得标题 */
    private String title;

    /** 心得内容（富文本） */
    private String content;

    /** 图片列表(JSON) */
    private String images;

    /** 状态：0-待审核，1-已发布，2-已拒绝 */
    private Integer status;

    /** 审核备注 */
    private String auditRemark;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 是否精选 */
    private Integer isFeatured;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // ========== 状态常量 ==========
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_PUBLISHED = 1;
    public static final Integer STATUS_REJECTED = 2;
}

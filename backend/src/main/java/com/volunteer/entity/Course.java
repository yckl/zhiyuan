package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 培训课程实体
 * 对应表: course
 */
@Data
@Accessors(chain = true)
@TableName("course")
public class Course implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程标题 */
    private String title;

    /** 封面图片 */
    private String coverImage;

    /** 课程分类 */
    private String category;

    /** 讲师/主讲人 */
    private String instructor;

    /** 课程摘要 */
    private String summary;

    /** 课程内容 */
    private String content;

    /** 视频地址 */
    private String videoUrl;

    /** 时长(分钟) */
    private Integer duration;

    /** 学分/学时 */
    private BigDecimal creditHours;

    /** 难度:1-初级,2-中级,3-高级 */
    private Integer difficulty;

    /** 是否必修 */
    private Integer isRequired;

    /** 观看次数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 考试及格奖励积分 */
    @TableField(exist = false)
    private Integer examRewardPoints;

    /** 状态:0-下架,1-上架 */
    private Integer status;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

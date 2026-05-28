package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程学习进度实体
 * 用于记录志愿者的课程学习进度，确保必须完成学习才能参加考试
 * 对应表: course_progress
 */
@Data
@Accessors(chain = true)
@TableName("course_progress")
public class CourseProgress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long volunteerId;

    /** 课程ID */
    private Long courseId;

    /** 学习进度(0-100%) */
    private Integer progress;

    /** 已学习时长(秒) */
    private Integer studyDuration;

    /** 课程总时长(秒) - 用于校验 */
    private Integer totalDuration;

    /** 最后学习位置(秒) - 视频播放位置 */
    private Integer lastPosition;

    /** 最后学习时间 */
    private LocalDateTime lastStudyTime;

    /** 是否已完成学习 */
    private Integer isCompleted;

    /** 完成时间 */
    private LocalDateTime completedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // 进度阈值：必须学习至少80%才能参加考试
    public static final int MIN_PROGRESS_FOR_EXAM = 80;
}

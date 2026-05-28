package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程浏览记录实体
 */
@Data
@Accessors(chain = true)
@TableName("course_view_log")
public class CourseViewLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 课程ID */
    private Long courseId;

    /** 浏览时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime viewTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

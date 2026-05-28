package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;
    private String operatorName;
    private String role; // e.g., 'ADMIN', 'ORGANIZER'
    private String operation; // Description of action
    private String path; // Request URI
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.volunteer.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseExamRecordVO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Integer score;
    private Integer totalScore;
    private Integer passed;
    private LocalDateTime submitTime;
}

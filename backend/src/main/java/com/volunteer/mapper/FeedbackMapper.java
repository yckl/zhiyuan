package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}

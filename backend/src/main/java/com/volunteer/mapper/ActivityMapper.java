package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}

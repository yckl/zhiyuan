package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿者Mapper接口
 */
@Mapper
public interface VolunteerMapper extends BaseMapper<Volunteer> {
}

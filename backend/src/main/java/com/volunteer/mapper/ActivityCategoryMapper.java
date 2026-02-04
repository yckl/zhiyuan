package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ActivityCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动分类Mapper接口
 */
@Mapper
public interface ActivityCategoryMapper extends BaseMapper<ActivityCategory> {
}

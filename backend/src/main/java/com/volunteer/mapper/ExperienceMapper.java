package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Experience;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface ExperienceMapper extends BaseMapper<Experience> {
    Page<Experience> selectExperienceList(Page<Experience> pageParam, @Param("type") String type,
            @Param("userId") Long userId, @Param("keyword") String keyword);
}

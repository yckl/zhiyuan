package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Organizer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 组织者Mapper接口
 */
@Mapper
public interface OrganizerMapper extends BaseMapper<Organizer> {

    /**
     * 根据用户ID查询组织者
     */
    @Select("SELECT * FROM organizer WHERE user_id = #{userId} AND is_deleted = 0")
    Organizer selectByUserId(Long userId);
}

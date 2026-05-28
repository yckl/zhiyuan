package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.UserLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户点赞Mapper
 */
@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {
}

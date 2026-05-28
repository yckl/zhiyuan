package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

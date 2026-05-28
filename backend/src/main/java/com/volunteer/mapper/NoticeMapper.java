package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 增加公告浏览次数
     * 
     * @param id 公告ID
     */
    @Update("UPDATE tongzhi SET view_count = COALESCE(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
}

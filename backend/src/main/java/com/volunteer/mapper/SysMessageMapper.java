package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内信 Mapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    @org.apache.ibatis.annotations.Select("SELECT title, content, MIN(create_time) as createTime, COUNT(*) as totalCount, SUM(is_read) as readCount "
            +
            "FROM sys_message " +
            "WHERE sender_id = #{senderId} AND is_deleted = 0 " +
            "GROUP BY title, content, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') " +
            "ORDER BY createTime DESC")
    com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> getBroadcastHistory(
            com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO> page,
            @org.apache.ibatis.annotations.Param("senderId") Long senderId);
}

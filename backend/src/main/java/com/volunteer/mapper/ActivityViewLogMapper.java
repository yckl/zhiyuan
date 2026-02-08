package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ActivityViewLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ActivityViewLogMapper extends BaseMapper<ActivityViewLog> {

        /**
         * 查询用户最近浏览的活动ID列表
         * 
         * @param userId 用户ID
         * @param since  时间起
         * @param limit  条数
         */
        @Select("SELECT activity_id FROM activity_view_log " +
                        "WHERE user_id = #{userId} AND view_time >= #{since} AND is_deleted = 0 " +
                        "GROUP BY activity_id " +
                        "ORDER BY MAX(view_time) DESC LIMIT #{limit}")
        List<Long> selectRecentViewedActivityIds(@Param("userId") Long userId,
                        @Param("since") LocalDateTime since,
                        @Param("limit") int limit);
}

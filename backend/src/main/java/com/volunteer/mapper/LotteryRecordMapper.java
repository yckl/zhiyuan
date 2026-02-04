package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.LotteryRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 抽奖记录 Mapper
 */
@Mapper
public interface LotteryRecordMapper extends BaseMapper<LotteryRecord> {

    /**
     * 获取用户抽奖记录
     */
    @Select("SELECT * FROM lottery_record WHERE volunteer_id = #{volunteerId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<LotteryRecord> getRecentRecords(@Param("volunteerId") Long volunteerId, @Param("limit") int limit);

    /**
     * 统计用户今日抽奖次数
     */
    @Select("SELECT COUNT(*) FROM lottery_record WHERE volunteer_id = #{volunteerId} AND DATE(create_time) = CURDATE() AND is_deleted = 0")
    int countTodayDraws(@Param("volunteerId") Long volunteerId);
}

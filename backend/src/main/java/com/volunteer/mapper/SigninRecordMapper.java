package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.SigninRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;

/**
 * 签到记录 Mapper
 */
@Mapper
public interface SigninRecordMapper extends BaseMapper<SigninRecord> {

    /**
     * 查询指定日期的签到记录
     */
    @Select("SELECT * FROM signin_record WHERE volunteer_id = #{volunteerId} AND signin_date = #{date} AND is_deleted = 0 LIMIT 1")
    SigninRecord findByVolunteerAndDate(@Param("volunteerId") Long volunteerId, @Param("date") LocalDate date);

    /**
     * 查询昨日签到记录
     */
    @Select("SELECT * FROM signin_record WHERE volunteer_id = #{volunteerId} AND signin_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND is_deleted = 0 LIMIT 1")
    SigninRecord findYesterdayRecord(@Param("volunteerId") Long volunteerId);

    /**
     * 获取最近一次签到记录
     */
    @Select("SELECT * FROM signin_record WHERE volunteer_id = #{volunteerId} AND is_deleted = 0 ORDER BY signin_date DESC LIMIT 1")
    SigninRecord findLatestRecord(@Param("volunteerId") Long volunteerId);

    /**
     * 统计本月签到天数
     */
    @Select("SELECT COUNT(*) FROM signin_record WHERE volunteer_id = #{volunteerId} " +
            "AND YEAR(signin_date) = YEAR(CURDATE()) AND MONTH(signin_date) = MONTH(CURDATE()) AND is_deleted = 0")
    int countMonthlySignins(@Param("volunteerId") Long volunteerId);

    /**
     * 统计总签到天数
     */
    @Select("SELECT COUNT(*) FROM signin_record WHERE volunteer_id = #{volunteerId} AND is_deleted = 0")
    int countTotalSignins(@Param("volunteerId") Long volunteerId);
}

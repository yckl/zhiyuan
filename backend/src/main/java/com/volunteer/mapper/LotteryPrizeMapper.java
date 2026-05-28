package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.LotteryPrize;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 抽奖奖品 Mapper
 */
@Mapper
public interface LotteryPrizeMapper extends BaseMapper<LotteryPrize> {

    /**
     * 获取所有启用的奖品（按排序）
     */
    @Select("SELECT * FROM lottery_prize WHERE status = 1 AND is_deleted = 0 ORDER BY sort_order ASC")
    List<LotteryPrize> getActivePrizes();

    /**
     * 增加中奖次数
     */
    @Update("UPDATE lottery_prize SET won_count = won_count + 1, update_time = NOW() WHERE id = #{prizeId}")
    int increaseWonCount(@Param("prizeId") Long prizeId);

    /**
     * 查询今日某奖品的中奖次数
     */
    @Select("SELECT COUNT(*) FROM lottery_record WHERE prize_id = #{prizeId} AND DATE(create_time) = #{date} AND is_won = 1 AND is_deleted = 0")
    int countTodayWins(@Param("prizeId") Long prizeId, @Param("date") LocalDate date);
}

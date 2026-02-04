package com.volunteer.service;

import com.volunteer.entity.LotteryPrize;
import com.volunteer.entity.SigninRecord;
import com.volunteer.vo.LotteryResultVO;
import com.volunteer.vo.SigninResultVO;

import java.util.List;

/**
 * 签到与转盘服务接口
 */
public interface CheckinLotteryService {

    // ==================== 签到功能 ====================

    /**
     * 每日签到
     */
    SigninResultVO dailySignin();

    /**
     * 获取签到状态（今日是否已签、连续天数等）
     */
    SigninResultVO getSigninStatus();

    /**
     * 补签（消耗补签卡）
     * 
     * @param dateStr 补签日期，格式 yyyy-MM-dd，如果为空则默认为昨天
     */
    SigninResultVO makeupSignin(String dateStr);

    /**
     * 获取本月签到日历
     */
    List<SigninRecord> getMonthlyCalendar(int year, int month);

    // ==================== 转盘抽奖 ====================

    /**
     * 获取奖品列表（转盘展示）
     */
    List<LotteryPrize> getPrizeList();

    /**
     * 抽奖
     * 
     * @param pointsCost 消耗积分
     */
    LotteryResultVO spinLottery(int pointsCost);

    /**
     * 获取抽奖记录
     */
    List<?> getLotteryRecords(int limit);
}

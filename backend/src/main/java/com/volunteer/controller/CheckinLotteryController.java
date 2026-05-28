package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.LotteryPrize;
import com.volunteer.entity.SigninRecord;
import com.volunteer.service.CheckinLotteryService;
import com.volunteer.vo.LotteryResultVO;
import com.volunteer.vo.SigninResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 签到与转盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Tag(name = "签到与转盘", description = "每日签到、补签、幸运转盘")
public class CheckinLotteryController {

    private final CheckinLotteryService service;

    // ==================== 签到相关 ====================

    /**
     * 每日签到
     */
    @PostMapping("/signin")
    @Operation(summary = "每日签到", description = "执行每日签到，获取积分奖励")
    public Result<SigninResultVO> dailySignin() {
        try {
            SigninResultVO result = service.dailySignin();
            if (result.isSuccess()) {
                return Result.success(result.getMessage(), result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("签到失败:", e);
            return Result.error("签到失败: " + e.getMessage());
        }
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/status")
    @Operation(summary = "签到状态", description = "获取今日签到状态、连续天数等信息")
    public Result<SigninResultVO> getSigninStatus() {
        try {
            SigninResultVO result = service.getSigninStatus();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取签到状态失败:", e);
            return Result.error("获取签到状态失败: " + e.getMessage());
        }
    }

    /**
     * 补签
     */
    @PostMapping("/makeup")
    @Operation(summary = "补签", description = "使用补签卡补签指定日期（默认昨日）")
    public Result<SigninResultVO> makeupSignin(@RequestParam(required = false) String date) {
        try {
            SigninResultVO result = service.makeupSignin(date);
            if (result.isSuccess()) {
                return Result.success(result.getMessage(), result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("补签失败:", e);
            return Result.error("补签失败: " + e.getMessage());
        }
    }

    /**
     * 获取本月签到日历
     */
    @GetMapping("/calendar")
    @Operation(summary = "签到日历", description = "获取指定月份的签到记录")
    public Result<List<SigninRecord>> getMonthlyCalendar(
            @Parameter(description = "年份") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份") @RequestParam(required = false) Integer month) {
        try {
            if (year == null)
                year = LocalDate.now().getYear();
            if (month == null)
                month = LocalDate.now().getMonthValue();

            List<SigninRecord> records = service.getMonthlyCalendar(year, month);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取签到日历失败:", e);
            return Result.error("获取签到日历失败: " + e.getMessage());
        }
    }

    // ==================== 转盘抽奖 ====================

    /**
     * 获取奖品列表
     */
    @GetMapping("/lottery/prizes")
    @Operation(summary = "奖品列表", description = "获取转盘奖品列表")
    public Result<List<LotteryPrize>> getPrizeList() {
        try {
            List<LotteryPrize> prizes = service.getPrizeList();
            return Result.success(prizes);
        } catch (Exception e) {
            log.error("获取奖品列表失败:", e);
            return Result.error("获取奖品列表失败: " + e.getMessage());
        }
    }

    /**
     * 抽奖
     */
    @PostMapping("/lottery/spin")
    @Operation(summary = "幸运抽奖", description = "消耗积分进行抽奖")
    public Result<LotteryResultVO> spinLottery(@RequestBody(required = false) Map<String, Integer> params) {
        try {
            int pointsCost = params != null && params.containsKey("pointsCost") ? params.get("pointsCost") : 10;
            LotteryResultVO result = service.spinLottery(pointsCost);
            if (result.isSuccess()) {
                return Result.success(result.getMessage(), result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("抽奖失败:", e);
            return Result.error("抽奖失败: " + e.getMessage());
        }
    }

    /**
     * 获取抽奖记录
     */
    @GetMapping("/lottery/records")
    @Operation(summary = "抽奖记录", description = "获取最近抽奖记录")
    public Result<List<?>> getLotteryRecords(
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "20") int limit) {
        try {
            List<?> records = service.getLotteryRecords(limit);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取抽奖记录失败:", e);
            return Result.error("获取抽奖记录失败: " + e.getMessage());
        }
    }
}

package com.volunteer.vo;

import com.volunteer.entity.SigninRecord;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到结果 VO
 */
@Data
public class SigninResultVO {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 今日是否已签 */
    private boolean signedToday;

    /** 连续签到天数 */
    private int continuousDays;

    /** 本次获得积分 */
    private int pointsEarned;

    /** 当前可用积分 */
    private int availablePoints;

    /** 本月签到天数 */
    private int monthlySignins;

    /** 总签到天数 */
    private int totalSignins;

    /** 是否可以补签 */
    private boolean canMakeup;

    /** 补签卡数量 */
    private int makeupCardCount;

    /** 签到日期 */
    private LocalDate signinDate;

    /** 本月签到记录 */
    private List<SigninRecord> monthlyRecords;

    public static SigninResultVO success(String message, int points, int continuousDays) {
        SigninResultVO vo = new SigninResultVO();
        vo.setSuccess(true);
        vo.setMessage(message);
        vo.setPointsEarned(points);
        vo.setContinuousDays(continuousDays);
        vo.setSignedToday(true);
        vo.setSigninDate(LocalDate.now());
        return vo;
    }

    public static SigninResultVO fail(String message) {
        SigninResultVO vo = new SigninResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}

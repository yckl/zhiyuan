package com.volunteer.vo;

import com.volunteer.entity.LotteryPrize;
import lombok.Data;

/**
 * 抽奖结果 VO
 */
@Data
public class LotteryResultVO {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 是否中奖 */
    private boolean won;

    /** 中奖奖品 */
    private LotteryPrize prize;

    /** 奖品索引（转盘停止位置） */
    private int prizeIndex;

    /** 奖品名称 */
    private String prizeName;

    /** 奖品类型 */
    private Integer prizeType;

    /** 奖品价值 */
    private Integer prizeValue;

    /** 剩余积分 */
    private int remainingPoints;

    public static LotteryResultVO win(LotteryPrize prize, int prizeIndex, int remainingPoints) {
        LotteryResultVO vo = new LotteryResultVO();
        vo.setSuccess(true);
        vo.setWon(true);
        vo.setPrize(prize);
        vo.setPrizeIndex(prizeIndex);
        vo.setPrizeName(prize.getName());
        vo.setPrizeType(prize.getPrizeType());
        vo.setPrizeValue(prize.getPrizeValue());
        vo.setRemainingPoints(remainingPoints);
        vo.setMessage("恭喜中奖: " + prize.getName());
        return vo;
    }

    public static LotteryResultVO lose(int remainingPoints) {
        LotteryResultVO vo = new LotteryResultVO();
        vo.setSuccess(true);
        vo.setWon(false);
        vo.setRemainingPoints(remainingPoints);
        vo.setMessage("很遗憾，未中奖");
        return vo;
    }

    public static LotteryResultVO fail(String message) {
        LotteryResultVO vo = new LotteryResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}

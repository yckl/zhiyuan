package com.volunteer.vo;

import com.volunteer.entity.MallOrder;
import lombok.Data;

/**
 * 购买结果 VO
 */
@Data
public class BuyResultVO {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 订单信息 */
    private MallOrder order;

    /** 核销码（实物商品） */
    private String pickupCode;

    /** 虚拟商品内容（虚拟商品） */
    private String virtualContent;

    /** 剩余积分 */
    private Integer remainingPoints;

    public static BuyResultVO success(MallOrder order, Integer remainingPoints) {
        BuyResultVO vo = new BuyResultVO();
        vo.setSuccess(true);
        vo.setMessage("购买成功");
        vo.setOrder(order);
        vo.setRemainingPoints(remainingPoints);
        if (order.getPickupCode() != null) {
            vo.setPickupCode(order.getPickupCode());
        }
        return vo;
    }

    public static BuyResultVO fail(String message) {
        BuyResultVO vo = new BuyResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}

package com.volunteer.service;

import com.volunteer.entity.MallGoods;
import com.volunteer.entity.MallOrder;
import com.volunteer.entity.UserProps;
import com.volunteer.vo.BuyResultVO;

import java.util.List;

/**
 * 积分商城服务接口
 */
public interface PointsMallService {

    /**
     * 获取商品列表
     */
    List<MallGoods> getGoodsList(String category, Integer status);

    /**
     * 获取商品详情
     */
    MallGoods getGoodsDetail(Long goodsId);

    /**
     * 购买商品
     * 
     * @param goodsId  商品ID
     * @param quantity 数量
     * @return 购买结果（包含核销码等信息）
     */
    BuyResultVO buyProduct(Long goodsId, int quantity);

    /**
     * 获取我的背包
     */
    List<UserProps> getMyBackpack();

    /**
     * 获取我的订单
     */
    List<MallOrder> getMyOrders();

    /**
     * 线下核销（管理员）
     * 
     * @param pickupCode 6位核销码
     * @return 核销结果
     */
    MallOrder verifyItem(String pickupCode);

    // ==================== 管理端接口 ====================

    /**
     * 分页获取所有商品 (管理员)
     */
    List<MallGoods> getAllGoodsList(String category, Integer status);

    /**
     * 保存或更新商品
     */
    void saveOrUpdateGoods(MallGoods goods);

    /**
     * 删除商品
     */
    void deleteGoods(Long id);

    /**
     * 分页查询所有兑换订单 (管理员)
     */
    List<MallOrder> getAllOrders();
}

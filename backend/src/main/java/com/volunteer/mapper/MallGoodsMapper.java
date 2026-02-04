package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.MallGoods;
import org.apache.ibatis.annotations.*;

/**
 * 商品 Mapper
 */
@Mapper
public interface MallGoodsMapper extends BaseMapper<MallGoods> {

    /**
     * 扣减库存（乐观锁）
     */
    @Update("UPDATE mall_goods SET stock = stock - #{quantity}, sold_count = sold_count + #{quantity}, update_time = NOW() "
            +
            "WHERE id = #{goodsId} AND (stock = -1 OR stock >= #{quantity}) AND is_deleted = 0")
    int decreaseStock(@Param("goodsId") Long goodsId, @Param("quantity") int quantity);

    /**
     * 恢复库存
     */
    @Update("UPDATE mall_goods SET stock = stock + #{quantity}, sold_count = sold_count - #{quantity}, update_time = NOW() "
            +
            "WHERE id = #{goodsId} AND stock != -1 AND is_deleted = 0")
    int restoreStock(@Param("goodsId") Long goodsId, @Param("quantity") int quantity);

    /**
     * 查询用户已购买某商品的数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM mall_order " +
            "WHERE volunteer_id = #{volunteerId} AND goods_id = #{goodsId} AND status != 3 AND is_deleted = 0")
    int getUserPurchaseCount(@Param("volunteerId") Long volunteerId, @Param("goodsId") Long goodsId);
}

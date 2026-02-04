package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.MallOrder;
import org.apache.ibatis.annotations.*;

/**
 * 订单 Mapper
 */
@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    /**
     * 根据核销码查询订单
     */
    @Select("SELECT * FROM mall_order WHERE pickup_code = #{pickupCode} AND is_deleted = 0 LIMIT 1")
    MallOrder findByPickupCode(@Param("pickupCode") String pickupCode);

    /**
     * 核销订单
     */
    @Update("UPDATE mall_order SET status = 1, complete_time = NOW(), update_time = NOW() " +
            "WHERE pickup_code = #{pickupCode} AND status = 0 AND is_deleted = 0")
    int verifyOrder(@Param("pickupCode") String pickupCode);
}

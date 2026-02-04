package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.MallGoods;
import com.volunteer.entity.MallOrder;
import com.volunteer.entity.UserProps;
import com.volunteer.service.PointsMallService;
import com.volunteer.vo.BuyResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分商城控制器
 */
@Slf4j
@RestController
@RequestMapping("/mall")
@RequiredArgsConstructor
@Tag(name = "积分商城", description = "商品列表、购买、背包、核销")
public class PointsMallController {

    private final PointsMallService mallService;

    // ==================== 商品相关 ====================

    /**
     * 获取商品列表
     */
    @GetMapping("/goods/list")
    @Operation(summary = "商品列表", description = "获取积分商城商品列表")
    public Result<List<MallGoods>> getGoodsList(
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "状态: 0-下架, 1-上架") @RequestParam(required = false) Integer status) {
        try {
            List<MallGoods> list = mallService.getGoodsList(category, status);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取商品列表失败:", e);
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/goods/{id}")
    @Operation(summary = "商品详情", description = "获取商品详细信息")
    public Result<MallGoods> getGoodsDetail(@PathVariable Long id) {
        try {
            MallGoods goods = mallService.getGoodsDetail(id);
            if (goods == null) {
                return Result.error("商品不存在");
            }
            return Result.success(goods);
        } catch (Exception e) {
            log.error("获取商品详情失败:", e);
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    // ==================== 购买相关 ====================

    /**
     * 购买商品
     */
    @PostMapping("/buy")
    @Operation(summary = "购买商品", description = "使用积分购买商品")
    public Result<BuyResultVO> buyProduct(@RequestBody Map<String, Object> params) {
        try {
            Long goodsId = Long.valueOf(params.get("goodsId").toString());
            int quantity = params.get("quantity") != null ? Integer.parseInt(params.get("quantity").toString()) : 1;

            BuyResultVO result = mallService.buyProduct(goodsId, quantity);

            if (result.isSuccess()) {
                return Result.success(result.getMessage(), result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("购买商品失败:", e);
            return Result.error("购买失败: " + e.getMessage());
        }
    }

    // ==================== 背包相关 ====================

    /**
     * 获取我的背包
     */
    @GetMapping("/backpack")
    @Operation(summary = "我的背包", description = "获取用户道具背包")
    public Result<List<UserProps>> getMyBackpack() {
        try {
            List<UserProps> backpack = mallService.getMyBackpack();
            return Result.success(backpack);
        } catch (Exception e) {
            log.error("获取背包失败:", e);
            return Result.error("获取背包失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的订单
     */
    @GetMapping("/orders")
    @Operation(summary = "我的订单", description = "获取用户的兑换订单列表")
    public Result<List<MallOrder>> getMyOrders() {
        try {
            List<MallOrder> orders = mallService.getMyOrders();
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取订单失败:", e);
            return Result.error("获取订单失败: " + e.getMessage());
        }
    }

    // ==================== 核销相关（管理员） ====================

    /**
     * 线下核销
     */
    @PostMapping("/verify")
    @Operation(summary = "核销订单", description = "管理员使用6位核销码核销订单")
    public Result<MallOrder> verifyItem(@RequestBody Map<String, String> params) {
        try {
            String pickupCode = params.get("pickupCode");
            if (pickupCode == null || pickupCode.trim().isEmpty()) {
                return Result.error("请输入核销码");
            }

            MallOrder order = mallService.verifyItem(pickupCode.trim().toUpperCase());
            return Result.success("核销成功", order);
        } catch (Exception e) {
            log.error("核销失败:", e);
            return Result.error(e.getMessage());
        }
    }
}

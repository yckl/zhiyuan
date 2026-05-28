package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.Result;
import com.volunteer.entity.MallGoods;
import com.volunteer.entity.MallOrder;
import com.volunteer.mapper.MallGoodsMapper;
import com.volunteer.mapper.MallOrderMapper;
import com.volunteer.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/mall")
@RequiredArgsConstructor
@Tag(name = "管理员积分商城", description = "商品管理与兑换记录")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointsMallController {

    private final MallGoodsMapper goodsMapper;
    private final MallOrderMapper orderMapper;
    private final SysMessageService sysMessageService;

    // ================= Product Management =================

    @GetMapping("/product/list")
    @Operation(summary = "商品列表")
    public Result<IPage<MallGoods>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<MallGoods> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MallGoods> query = new LambdaQueryWrapper<MallGoods>()
                .eq(MallGoods::getIsDeleted, 0)
                .orderByDesc(MallGoods::getSortOrder)
                .orderByDesc(MallGoods::getCreateTime);

        if (name != null && !name.isBlank()) {
            query.like(MallGoods::getName, name);
        }
        if (status != null) {
            query.eq(MallGoods::getStatus, status);
        }

        return Result.success(goodsMapper.selectPage(pageParam, query));
    }

    @PostMapping("/product/add")
    @Operation(summary = "新增商品")
    public Result<String> addProduct(@RequestBody MallGoods goods) {
        goods.setId(null);
        // Default values
        if (goods.getStatus() == null)
            goods.setStatus(0); // Default off-shelf
        if (goods.getSortOrder() == null)
            goods.setSortOrder(0);

        goodsMapper.insert(goods);
        return Result.success("新增成功");
    }

    @PutMapping("/product/update")
    @Operation(summary = "更新商品")
    public Result<String> updateProduct(@RequestBody MallGoods goods) {
        goodsMapper.updateById(goods);
        return Result.success("更新成功");
    }

    @PutMapping("/product/status")
    @Operation(summary = "上架/下架商品")
    public Result<String> updateProductStatus(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = (Integer) body.get("status");

        MallGoods goods = goodsMapper.selectById(id);
        if (goods == null)
            return Result.error("商品不存在");

        goods.setStatus(status);
        goodsMapper.updateById(goods);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/product/delete/{id}")
    @Operation(summary = "删除商品")
    public Result<String> deleteProduct(@PathVariable Long id) {
        goodsMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ================= Exchange Records =================

    @GetMapping("/exchange/list")
    @Operation(summary = "兑换记录列表")
    public Result<IPage<MallOrder>> listOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String pickupCode) {
        Page<MallOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MallOrder> query = new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getIsDeleted, 0)
                .orderByDesc(MallOrder::getCreateTime);

        if (orderNo != null && !orderNo.isBlank()) {
            query.like(MallOrder::getOrderNo, orderNo);
        }
        if (pickupCode != null && !pickupCode.isBlank()) {
            query.eq(MallOrder::getPickupCode, pickupCode);
        }

        IPage<MallOrder> orderPage = orderMapper.selectPage(pageParam, query);

        // Enrich receiver info logic usually done here or handled by logic.
        // MallOrder already has receiverName/Phone snapshot.
        // It has `volunteerId` if we need actual name/studentId if generic receiver
        // info is missing.

        return Result.success(orderPage);
    }

    @PutMapping("/exchange/deliver")
    @Operation(summary = "发放/核销商品")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deliverOrder(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());

        MallOrder order = orderMapper.selectById(id);
        if (order == null)
            return Result.error("订单不存在");
        if (order.getStatus() != MallOrder.OrderStatus.PENDING) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(MallOrder.OrderStatus.VERIFIED);
        orderMapper.updateById(order);

        // Notify User
        sysMessageService.sendMessage(
                order.getVolunteerId(),
                1L, // System
                "【积分商城】奖品发放通知",
                String.format("您兑换的商品【%s】已发放！请留意查收或已现场核销。", order.getGoodsName()),
                "SYSTEM");

        return Result.success("发放成功");
    }
}

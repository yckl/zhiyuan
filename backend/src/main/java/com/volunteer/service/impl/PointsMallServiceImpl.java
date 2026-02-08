package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.PointsMallService;
import com.volunteer.service.SysMessageService;
import com.volunteer.vo.BuyResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 积分商城服务实现
 * 核心逻辑：购买、背包、核销
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsMallServiceImpl implements PointsMallService {

    private final MallGoodsMapper goodsMapper;
    private final MallOrderMapper orderMapper;
    private final UserPropsMapper propsMapper;
    private final PointsRecordMapper recordMapper;
    private final VolunteerMapper volunteerMapper;
    private final SysMessageService messageService;

    @Override
    public List<MallGoods> getGoodsList(String category, Integer status) {
        LambdaQueryWrapper<MallGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallGoods::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(MallGoods::getStatus, status);
        } else {
            wrapper.eq(MallGoods::getStatus, 1); // 默认只查上架商品
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MallGoods::getCategory, category);
        }
        wrapper.orderByAsc(MallGoods::getSortOrder).orderByDesc(MallGoods::getCreateTime);
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public MallGoods getGoodsDetail(Long goodsId) {
        return goodsMapper.selectById(goodsId);
    }

    /**
     * 购买商品
     * 核心业务逻辑，使用事务控制
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuyResultVO buyProduct(Long goodsId, int quantity) {
        // 1. 获取当前用户
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return BuyResultVO.fail("请先登录");
        }

        // 获取志愿者信息
        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            return BuyResultVO.fail("志愿者信息不存在");
        }

        // 2. 查询商品
        MallGoods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getIsDeleted() == 1) {
            return BuyResultVO.fail("商品不存在");
        }
        if (goods.getStatus() != 1) {
            return BuyResultVO.fail("商品已下架");
        }

        // 3. 检查库存
        if (goods.getStock() != -1 && goods.getStock() < quantity) {
            return BuyResultVO.fail("库存不足，当前库存: " + goods.getStock());
        }

        // 4. 检查限购
        if (goods.getLimitPerUser() != null && goods.getLimitPerUser() > 0) {
            int purchased = goodsMapper.getUserPurchaseCount(volunteer.getId(), goodsId);
            if (purchased + quantity > goods.getLimitPerUser()) {
                return BuyResultVO.fail("超出限购数量，每人限购: " + goods.getLimitPerUser());
            }
        }

        // 5. 检查积分
        int totalCost = goods.getPointsPrice() * quantity;
        if (volunteer.getAvailablePoints() == null || volunteer.getAvailablePoints() < totalCost) {
            return BuyResultVO.fail("积分不足，需要: " + totalCost + "，当前: " + volunteer.getAvailablePoints());
        }

        // 6. 扣减库存（乐观锁）
        int affected = goodsMapper.decreaseStock(goodsId, quantity);
        if (affected == 0) {
            return BuyResultVO.fail("库存不足，请稍后重试");
        }

        // 7. 扣减用户积分
        int newBalance = volunteer.getAvailablePoints() - totalCost;
        volunteer.setAvailablePoints(newBalance);
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        // 8. 创建积分记录
        PointsRecord record = new PointsRecord();
        record.setVolunteerId(volunteer.getId());
        record.setPoints(-totalCost);
        record.setBalance(newBalance);
        record.setType(PointsRecord.Type.EXCHANGE);
        record.setBizId(goodsId);
        record.setDescription("兑换商品: " + goods.getName() + " x" + quantity);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        recordMapper.insert(record);

        // 9. 创建订单
        MallOrder order = new MallOrder();
        order.setOrderNo(generateOrderNo());
        order.setVolunteerId(volunteer.getId());
        order.setGoodsId(goodsId);
        order.setGoodsName(goods.getName());
        order.setGoodsImage(goods.getCoverImage());
        order.setQuantity(quantity);
        order.setPointsCost(totalCost);
        order.setStatus(MallOrder.OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setIsDeleted(0);

        // 10. 根据商品类型处理
        if (goods.getGoodsType() == MallGoods.GoodsType.VIRTUAL) {
            // 虚拟商品：直接放入背包
            addToBackpack(volunteer.getId(), goods, quantity);
            order.setStatus(MallOrder.OrderStatus.COMPLETED);
            order.setCompleteTime(LocalDateTime.now());
            log.info("虚拟商品已放入背包: userId={}, goods={}", userId, goods.getName());
        } else {
            // 实物商品：生成6位核销码
            String pickupCode = generatePickupCode();
            order.setPickupCode(pickupCode);
            log.info("实物商品核销码已生成: code={}, goods={}", pickupCode, goods.getName());
        }

        orderMapper.insert(order);

        BuyResultVO result = BuyResultVO.success(order, newBalance);
        if (goods.getGoodsType() == MallGoods.GoodsType.VIRTUAL) {
            result.setVirtualContent(goods.getVirtualContent());
        }

        // 11. 发送站内信通知
        String notifyTitle = "🎉 兑换成功通知";
        String notifyContent = String.format(
                "恭喜！您已成功兑换「%s」x%d，消耗 %d 积分。%s",
                goods.getName(),
                quantity,
                totalCost,
                goods.getGoodsType() == MallGoods.GoodsType.VIRTUAL
                        ? "虚拟商品已放入您的背包。"
                        : "实物商品请凭核销码到指定地点领取。");
        messageService.sendMessage(userId, null, notifyTitle, notifyContent, SysMessage.TYPE_MALL);

        log.info("用户 {} 成功购买商品 {} x{}, 消耗积分 {}", userId, goods.getName(), quantity, totalCost);
        return result;
    }

    /**
     * 将虚拟商品添加到背包
     */
    private void addToBackpack(Long volunteerId, MallGoods goods, int quantity) {
        // 检查是否已有相同道具
        UserProps existing = propsMapper.findByVolunteerAndProp(
                volunteerId, goods.getId(), UserProps.PropType.GOODS);

        if (existing != null) {
            // 已有则累加数量
            propsMapper.increaseQuantity(existing.getId(), quantity);
        } else {
            // 没有则新增
            UserProps props = new UserProps();
            props.setVolunteerId(volunteerId);
            props.setPropType(UserProps.PropType.GOODS);
            props.setPropId(goods.getId());
            props.setPropName(goods.getName());
            props.setPropImage(goods.getCoverImage());
            props.setPropContent(goods.getVirtualContent());
            props.setQuantity(quantity);
            props.setStatus(UserProps.Status.UNUSED);
            props.setSource(UserProps.Source.EXCHANGE);
            props.setCreateTime(LocalDateTime.now());
            props.setUpdateTime(LocalDateTime.now());
            props.setIsDeleted(0);
            propsMapper.insert(props);
        }
    }

    @Override
    public List<UserProps> getMyBackpack() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        return propsMapper.getUserBackpack(volunteer.getId());
    }

    @Override
    public List<MallOrder> getMyOrders() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            throw new RuntimeException("志愿者信息不存在");
        }

        return orderMapper.selectList(
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getVolunteerId, volunteer.getId())
                        .eq(MallOrder::getIsDeleted, 0)
                        .orderByDesc(MallOrder::getCreateTime));
    }

    /**
     * 线下核销（管理员）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrder verifyItem(String pickupCode) {
        if (pickupCode == null || pickupCode.length() != 6) {
            throw new RuntimeException("核销码格式错误，应为6位");
        }

        // 查询订单
        MallOrder order = orderMapper.findByPickupCode(pickupCode);
        if (order == null) {
            throw new RuntimeException("核销码无效或不存在");
        }

        // 检查状态
        if (order.getStatus() == MallOrder.OrderStatus.VERIFIED ||
                order.getStatus() == MallOrder.OrderStatus.COMPLETED) {
            throw new RuntimeException("该订单已核销");
        }
        if (order.getStatus() == MallOrder.OrderStatus.CANCELLED) {
            throw new RuntimeException("该订单已取消");
        }

        // 执行核销
        int affected = orderMapper.verifyOrder(pickupCode);
        if (affected == 0) {
            throw new RuntimeException("核销失败，请重试");
        }

        // 重新查询更新后的订单
        order = orderMapper.findByPickupCode(pickupCode);
        log.info("订单核销成功: orderNo={}, pickupCode={}", order.getOrderNo(), pickupCode);

        return order;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000; // 4位随机数
        return "PO" + timestamp + random;
    }

    /**
     * 生成6位随机核销码（字母+数字）
     */
    private String generatePickupCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除易混淆字符
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public List<MallGoods> getAllGoodsList(String category, Integer status) {
        LambdaQueryWrapper<MallGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallGoods::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(MallGoods::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MallGoods::getCategory, category);
        }
        wrapper.orderByAsc(MallGoods::getSortOrder).orderByDesc(MallGoods::getCreateTime);
        return goodsMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateGoods(MallGoods goods) {
        if (goods.getId() != null) {
            goods.setUpdateTime(LocalDateTime.now());
            goodsMapper.updateById(goods);
        } else {
            goods.setCreateTime(LocalDateTime.now());
            goods.setUpdateTime(LocalDateTime.now());
            goods.setIsDeleted(0);
            if (goods.getStatus() == null)
                goods.setStatus(1);
            goodsMapper.insert(goods);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id) {
        MallGoods goods = goodsMapper.selectById(id);
        if (goods != null) {
            goods.setIsDeleted(1);
            goods.setUpdateTime(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
    }

    @Override
    public List<MallOrder> getAllOrders() {
        return orderMapper.selectList(
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getIsDeleted, 0)
                        .orderByDesc(MallOrder::getCreateTime));
    }
}

package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import com.volunteer.security.SecurityUtils;
import com.volunteer.service.CheckinLotteryService;
import com.volunteer.vo.LotteryResultVO;
import com.volunteer.vo.SigninResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 签到与转盘服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinLotteryServiceImpl implements CheckinLotteryService {

    private final SigninRecordMapper signinMapper;
    private final LotteryPrizeMapper prizeMapper;
    private final LotteryRecordMapper lotteryRecordMapper;
    private final UserPropsMapper propsMapper;
    private final VolunteerMapper volunteerMapper;
    private final PointsRecordMapper pointsRecordMapper;

    // 签到积分配置
    private static final int BASE_POINTS = 5; // 基础积分
    private static final int BONUS_DAY_7 = 7; // 7天连续签到双倍
    private static final int BONUS_DAY_30 = 30; // 30天连续签到三倍
    private static final int LOTTERY_COST = 10; // 抽奖消耗积分
    private static final String MAKEUP_CARD = "MAKEUP_CARD"; // 补签卡类型

    // ==================== 签到功能 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SigninResultVO dailySignin() {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return SigninResultVO.fail("请先登录");
        }

        LocalDate today = LocalDate.now();

        // 检查今日是否已签
        SigninRecord todayRecord = signinMapper.findByVolunteerAndDate(volunteer.getId(), today);
        if (todayRecord != null) {
            return SigninResultVO.fail("今日已签到");
        }

        // 计算连续签到天数
        int continuousDays = calculateContinuousDays(volunteer.getId(), today);

        // 计算积分奖励
        int points = calculateSigninPoints(continuousDays);

        // 创建签到记录
        SigninRecord record = new SigninRecord();
        record.setVolunteerId(volunteer.getId());
        record.setSigninDate(today);
        record.setContinuousDays(continuousDays);
        record.setPointsEarned(points);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        signinMapper.insert(record);

        // 增加用户积分
        addPoints(volunteer, points, "每日签到奖励(连续" + continuousDays + "天)");

        log.info("用户 {} 签到成功, 连续{}天, 获得{}积分", volunteer.getId(), continuousDays, points);

        SigninResultVO result = SigninResultVO.success("签到成功！获得" + points + "积分", points, continuousDays);
        result.setAvailablePoints(volunteer.getAvailablePoints() + points);
        return result;
    }

    @Override
    public SigninResultVO getSigninStatus() {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return SigninResultVO.fail("请先登录");
        }

        SigninResultVO result = new SigninResultVO();
        result.setSuccess(true);

        LocalDate today = LocalDate.now();

        // 今日是否已签
        SigninRecord todayRecord = signinMapper.findByVolunteerAndDate(volunteer.getId(), today);
        result.setSignedToday(todayRecord != null);

        // 连续签到天数
        if (todayRecord != null) {
            result.setContinuousDays(todayRecord.getContinuousDays());
        } else {
            // 昨日是否签到决定连续天数
            SigninRecord yesterdayRecord = signinMapper.findYesterdayRecord(volunteer.getId());
            result.setContinuousDays(yesterdayRecord != null ? yesterdayRecord.getContinuousDays() : 0);
        }

        // 统计
        result.setMonthlySignins(signinMapper.countMonthlySignins(volunteer.getId()));
        result.setTotalSignins(signinMapper.countTotalSignins(volunteer.getId()));
        result.setAvailablePoints(volunteer.getAvailablePoints() != null ? volunteer.getAvailablePoints() : 0);

        // 是否可以补签（昨日未签且有补签卡）
        SigninRecord yesterdayRecord = signinMapper.findYesterdayRecord(volunteer.getId());
        int makeupCards = countMakeupCards(volunteer.getId());
        result.setCanMakeup(yesterdayRecord == null && makeupCards > 0);
        result.setMakeupCardCount(makeupCards);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SigninResultVO makeupSignin(String dateStr) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return SigninResultVO.fail("请先登录");
        }

        LocalDate makeupDate;
        if (dateStr == null || dateStr.isEmpty()) {
            makeupDate = LocalDate.now().minusDays(1);
        } else {
            try {
                makeupDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                return SigninResultVO.fail("无效的日期格式");
            }
        }

        if (makeupDate.isAfter(LocalDate.now())) {
            return SigninResultVO.fail("不能补登未来的日期");
        }
        if (makeupDate.equals(LocalDate.now())) {
            return SigninResultVO.fail("今日请点击正常签到");
        }

        // 检查当日是否已签
        SigninRecord existingRecord = signinMapper.findByVolunteerAndDate(volunteer.getId(), makeupDate);
        if (existingRecord != null) {
            return SigninResultVO.fail(makeupDate.toString() + " 已签到，无需补签");
        }

        // 检查补签卡
        UserProps makeupCard = findMakeupCard(volunteer.getId());
        if (makeupCard == null || makeupCard.getQuantity() <= 0) {
            return SigninResultVO.fail("补签卡不足");
        }

        // 消耗补签卡 - 使用更健壮的逻辑
        // 直接在数据库层面扣减库存，避免并发问题
        int rows = propsMapper.decreaseQuantity(makeupCard.getId(), 1);
        if (rows == 0) {
            // 如果扣减失败（可能因为库存不足），抛出异常回滚事务
            throw new RuntimeException("补签卡扣减失败，请重试");
        }

        // 如果扣减后库存为0，将其标记为删除（可选，视业务逻辑而定）
        // 这里为了简单，我们重新查询一次确认
        UserProps updatedProps = propsMapper.selectById(makeupCard.getId());
        if (updatedProps != null && updatedProps.getQuantity() <= 0) {
            updatedProps.setIsDeleted(1);
            propsMapper.updateById(updatedProps);
        }

        log.info("用户 {} 消耗补签卡成功, propsId: {}", volunteer, makeupCard.getId());

        // 计算连续天数（比较复杂，这里简化处理：基于前一天的连续天数+1，并可能影响后一天的连续天数）
        // 为了简单，我们只从前一天获取
        SigninRecord prevDayRecord = signinMapper.findByVolunteerAndDate(volunteer.getId(), makeupDate.minusDays(1));
        int continuousDays = (prevDayRecord != null ? prevDayRecord.getContinuousDays() : 0) + 1;

        // 创建补签记录
        int points = calculateSigninPoints(continuousDays);
        SigninRecord record = new SigninRecord();
        record.setVolunteerId(volunteer.getId());
        record.setSigninDate(makeupDate);
        record.setContinuousDays(continuousDays);
        record.setPointsEarned(points);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        signinMapper.insert(record);

        // 增加积分
        addPoints(volunteer, points, "补签奖励(" + makeupDate + ")");

        log.info("用户 {} 补签成功, 日期:{}, 获得{}积分", volunteer.getId(), makeupDate, points);

        SigninResultVO result = SigninResultVO.success("补签成功！获得" + points + "积分", points, continuousDays);
        result.setAvailablePoints(volunteer.getAvailablePoints() + points);
        result.setSigninDate(makeupDate);
        return result;
    }

    @Override
    public List<SigninRecord> getMonthlyCalendar(int year, int month) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return new ArrayList<>();
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        // 为了兼容前端 42 格日历（包含上月/下月补位），范围左右各扩 7 天
        LocalDate startDate = yearMonth.atDay(1).minusDays(7);
        LocalDate endDate = yearMonth.atEndOfMonth().plusDays(7);

        log.info("查询签到日历(含补位): 用户 {}, 范围 {} 至 {}", volunteer.getId(), startDate, endDate);

        return signinMapper.selectList(
                new LambdaQueryWrapper<SigninRecord>()
                        .eq(SigninRecord::getVolunteerId, volunteer.getId())
                        .ge(SigninRecord::getSigninDate, startDate)
                        .le(SigninRecord::getSigninDate, endDate)
                        .eq(SigninRecord::getIsDeleted, 0)
                        .orderByAsc(SigninRecord::getSigninDate));
    }

    // ==================== 转盘抽奖 ====================

    @Override
    public List<LotteryPrize> getPrizeList() {
        return prizeMapper.getActivePrizes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryResultVO spinLottery(int pointsCost) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return LotteryResultVO.fail("请先登录");
        }

        // 默认消耗10积分
        if (pointsCost <= 0) {
            pointsCost = LOTTERY_COST;
        }

        // 检查积分
        if (volunteer.getAvailablePoints() == null || volunteer.getAvailablePoints() < pointsCost) {
            return LotteryResultVO.fail("积分不足，需要" + pointsCost + "积分");
        }

        // 获取奖品列表
        List<LotteryPrize> prizes = prizeMapper.getActivePrizes();
        if (prizes.isEmpty()) {
            return LotteryResultVO.fail("暂无奖品");
        }

        // 扣除积分
        volunteer.setAvailablePoints(volunteer.getAvailablePoints() - pointsCost);
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);

        // 记录积分消耗
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setVolunteerId(volunteer.getId());
        pointsRecord.setPoints(-pointsCost);
        pointsRecord.setBalance(volunteer.getAvailablePoints());
        pointsRecord.setType(PointsRecord.Type.LOTTERY);
        pointsRecord.setDescription("幸运转盘抽奖");
        pointsRecord.setCreateTime(LocalDateTime.now());
        pointsRecord.setUpdateTime(LocalDateTime.now());
        pointsRecord.setIsDeleted(0);
        pointsRecordMapper.insert(pointsRecord);

        // 执行抽奖算法
        LotteryPrize wonPrize = runLotteryAlgorithm(prizes);

        // 创建抽奖记录
        LotteryRecord record = new LotteryRecord();
        record.setVolunteerId(volunteer.getId());
        record.setPointsCost(pointsCost);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);

        if (wonPrize != null) {
            // 特殊处理：如果是"谢谢参与"，视为未中奖，不发放奖励
            if (wonPrize.getName() != null && (wonPrize.getName().contains("谢谢") || wonPrize.getName().contains("遗憾")
                    || wonPrize.getName().contains("再接再厉"))) {
                // 记录未中奖（虽然逻辑上是选中了这个奖品，但业务上视为未中奖）
                record.setIsWon(0);
                record.setStatus(1);
                // 确保不调用 awardPrize
                lotteryRecordMapper.insert(record);
                log.info("用户 {} 抽中'{}'，视为未中奖", volunteer.getId(), wonPrize.getName());
                return LotteryResultVO.lose(volunteer.getAvailablePoints());
            }

            record.setPrizeId(wonPrize.getId());
            record.setPrizeName(wonPrize.getName());
            record.setPrizeType(wonPrize.getPrizeType());
            record.setPrizeValue(wonPrize.getPrizeValue());
            record.setIsWon(1);
            record.setStatus(1); // 自动领取

            // 发放奖励 (必须调用)
            awardPrize(volunteer, wonPrize);

            // 更新中奖次数
            prizeMapper.increaseWonCount(wonPrize.getId());

            lotteryRecordMapper.insert(record);

            int prizeIndex = prizes.indexOf(wonPrize);
            log.info("用户 {} 抽奖中奖: {}, 位置{}, 类型{}", volunteer.getId(), wonPrize.getName(), prizeIndex,
                    wonPrize.getPrizeType());

            // 确保返回正确的 prizeType 和 prizeValue 给前端
            LotteryResultVO result = LotteryResultVO.win(wonPrize, prizeIndex, volunteer.getAvailablePoints());
            // result.setPrizeType 已在 win 方法中设置
            return result;
        } else {
            record.setIsWon(0);
            record.setStatus(1);
            lotteryRecordMapper.insert(record);

            log.info("用户 {} 抽奖未中奖", volunteer.getId());
            return LotteryResultVO.lose(volunteer.getAvailablePoints());
        }
    }

    @Override
    public List<?> getLotteryRecords(int limit) {
        Volunteer volunteer = getCurrentVolunteer();
        if (volunteer == null) {
            return new ArrayList<>();
        }
        return lotteryRecordMapper.getRecentRecords(volunteer.getId(), limit);
    }

    // ==================== 私有方法 ====================

    /**
     * 抽奖算法
     * 根据概率随机选择奖品
     */
    private LotteryPrize runLotteryAlgorithm(List<LotteryPrize> prizes) {
        Random random = new Random();
        double randomValue = random.nextDouble();
        double cumulative = 0.0;

        LocalDate today = LocalDate.now();

        for (LotteryPrize prize : prizes) {
            // 检查每日限量
            if (prize.getDailyLimit() > 0) {
                int todayWins = prizeMapper.countTodayWins(prize.getId(), today);
                if (todayWins >= prize.getDailyLimit()) {
                    continue; // 跳过已达限量的奖品
                }
            }

            // 检查总限量
            if (prize.getTotalLimit() > 0 && prize.getWonCount() >= prize.getTotalLimit()) {
                continue; // 跳过已达总限量的奖品
            }

            cumulative += prize.getProbability().doubleValue();
            if (randomValue <= cumulative) {
                return prize;
            }
        }

        return null; // 未中奖
    }

    /**
     * 发放奖励
     */
    private void awardPrize(Volunteer volunteer, LotteryPrize prize) {
        if (prize.getPrizeType() == null) {
            log.error("奖品类型为空: {}", prize.getId());
            return;
        }

        // 容错处理：如果奖品名称包含"积分"，强制视为积分类型
        // 解决数据库可能存在的配置错误（例如50积分被配成了实物或虚拟道具）
        int effectiveType = prize.getPrizeType();
        if (prize.getName() != null && prize.getName().contains("积分")) {
            effectiveType = LotteryPrize.PrizeType.POINTS;
            // 如果 prizeValue 为 0，尝试从名称解析
            if (prize.getPrizeValue() == null || prize.getPrizeValue() == 0) {
                try {
                    String numStr = prize.getName().replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        prize.setPrizeValue(Integer.parseInt(numStr));
                        log.warn("从奖品名称 '{}' 解析出积分值: {}", prize.getName(), prize.getPrizeValue());
                    }
                } catch (Exception e) {
                    log.error("解析积分值失败", e);
                }
            }
        }

        switch (effectiveType) {
            case LotteryPrize.PrizeType.POINTS -> {
                // 积分奖励
                addPoints(volunteer, prize.getPrizeValue(), "抽奖获得积分");
            }
            case LotteryPrize.PrizeType.VIRTUAL -> {
                // 虚拟道具（如补签卡）
                addVirtualProp(volunteer.getId(), prize);
            }
            case LotteryPrize.PrizeType.PHYSICAL -> {
                // 实物奖励 - 记录待领取即可
                log.info("用户 {} 获得实物奖品: {}", volunteer.getId(), prize.getName());
            }
            default -> {
                log.warn("未知的奖品类型: {}", prize.getPrizeType());
            }
        }
    }

    /**
     * 添加虚拟道具到背包
     */
    private void addVirtualProp(Long volunteerId, LotteryPrize prize) {
        // 检查是否已有相同道具
        UserProps existing = propsMapper.findByVolunteerAndProp(
                volunteerId, prize.getId(), MAKEUP_CARD);

        if (existing != null) {
            propsMapper.increaseQuantity(existing.getId(), 1);
        } else {
            UserProps props = new UserProps();
            props.setVolunteerId(volunteerId);
            props.setPropType(MAKEUP_CARD);
            props.setPropId(prize.getId());
            props.setPropName(prize.getName());
            props.setPropImage(prize.getImage());
            props.setQuantity(1);
            props.setStatus(UserProps.Status.UNUSED);
            props.setSource(UserProps.Source.LOTTERY);
            props.setCreateTime(LocalDateTime.now());
            props.setUpdateTime(LocalDateTime.now());
            props.setIsDeleted(0);
            propsMapper.insert(props);
        }
    }

    /**
     * 计算连续签到天数
     */
    private int calculateContinuousDays(Long volunteerId, LocalDate today) {
        SigninRecord yesterdayRecord = signinMapper.findByVolunteerAndDate(volunteerId, today.minusDays(1));
        if (yesterdayRecord != null) {
            return yesterdayRecord.getContinuousDays() + 1;
        }
        return 1; // 新的连续周期
    }

    /**
     * 计算签到积分（阶梯奖励）
     */
    private int calculateSigninPoints(int continuousDays) {
        int multiplier = 1;
        if (continuousDays >= BONUS_DAY_30) {
            multiplier = 3; // 30天连续三倍
        } else if (continuousDays >= BONUS_DAY_7) {
            multiplier = 2; // 7天连续双倍
        }
        return BASE_POINTS * multiplier;
    }

    /**
     * 增加用户积分
     */
    private void addPoints(Volunteer volunteer, int points, String description) {
        log.info("准备增加积分. 用户ID: {}, 增加: {}, 描述: {}", volunteer.getId(), points, description);

        if (points <= 0) {
            log.warn("增加积分为0或负数，跳过: {}", points);
            return;
        }

        int oldBalance = volunteer.getAvailablePoints() != null ? volunteer.getAvailablePoints() : 0;
        int newBalance = oldBalance + points;
        int newTotal = (volunteer.getTotalPoints() != null ? volunteer.getTotalPoints() : 0) + points;

        // 更新内存对象
        volunteer.setAvailablePoints(newBalance);
        volunteer.setTotalPoints(newTotal);
        volunteer.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        volunteerMapper.updateById(volunteer);

        log.info("积分增加成功. 旧余额: {}, 新余额: {}", oldBalance, newBalance);

        // 记录积分流水
        PointsRecord record = new PointsRecord();
        record.setVolunteerId(volunteer.getId());
        record.setPoints(points);
        record.setBalance(newBalance);
        record.setType(PointsRecord.Type.SIGNIN);
        if (description.contains("抽奖")) {
            record.setType(PointsRecord.Type.LOTTERY);
        } else if (description.contains("补签")) {
            record.setType(PointsRecord.Type.SIGNIN); // 补签也算签到
        }
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        pointsRecordMapper.insert(record);
    }

    /**
     * 查找补签卡
     */
    private UserProps findMakeupCard(Long volunteerId) {
        return propsMapper.selectOne(
                new LambdaQueryWrapper<UserProps>()
                        .eq(UserProps::getVolunteerId, volunteerId)
                        .eq(UserProps::getIsDeleted, 0)
                        .gt(UserProps::getQuantity, 0)
                        .and(q -> q.eq(UserProps::getPropType, MAKEUP_CARD)
                                .or().like(UserProps::getPropName, "补签"))
                        .last("LIMIT 1"));
    }

    /**
     * 统计补签卡数量
     */
    private int countMakeupCards(Long volunteerId) {
        List<UserProps> cards = propsMapper.selectList(
                new LambdaQueryWrapper<UserProps>()
                        .eq(UserProps::getVolunteerId, volunteerId)
                        .eq(UserProps::getIsDeleted, 0)
                        .gt(UserProps::getQuantity, 0)
                        .and(q -> q.eq(UserProps::getPropType, MAKEUP_CARD)
                                .or().like(UserProps::getPropName, "补签")));
        return cards.stream().mapToInt(UserProps::getQuantity).sum();
    }

    /**
     * 获取当前志愿者
     */
    private Volunteer getCurrentVolunteer() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
            return null;

        return volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
    }
}

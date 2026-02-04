package com.volunteer.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 统计数据 Mapper
 */
@Mapper
public interface StatisticsMapper {

        /**
         * 统计志愿者总数
         */
        @Select("SELECT COUNT(*) FROM sys_user WHERE role = 'VOLUNTEER' AND is_deleted = 0")
        int countVolunteers();

        /**
         * 统计活动总数
         */
        @Select("SELECT COUNT(*) FROM activity WHERE is_deleted = 0")
        int countActivities();

        /**
         * 统计志愿服务总时长
         */
        @Select("SELECT COALESCE(SUM(total_hours), 0) FROM volunteer WHERE is_deleted = 0")
        double sumTotalHours();

        /**
         * 统计今日新增报名数
         */
        @Select("SELECT COUNT(*) FROM activity_registration WHERE DATE(create_time) = CURDATE() AND is_deleted = 0")
        int countTodayRegistrations();

        /**
         * 按分类统计活动数量（饼图数据）
         */
        @Select("SELECT c.name AS name, COUNT(a.id) AS value " +
                        "FROM activity a " +
                        "JOIN activity_category c ON a.category_id = c.id " +
                        "WHERE a.is_deleted = 0 AND c.is_deleted = 0 " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY value DESC")
        List<Map<String, Object>> countByCategory();

        /**
         * 按学院统计报名人数 Top 10（柱状图数据）
         */
        @Select("SELECT v.college, COUNT(r.id) AS cnt " +
                        "FROM activity_registration r " +
                        "JOIN volunteer v ON r.volunteer_id = v.id " +
                        "WHERE r.is_deleted = 0 AND v.is_deleted = 0 AND v.college IS NOT NULL AND v.college != '' " +
                        "GROUP BY v.college " +
                        "ORDER BY cnt DESC " +
                        "LIMIT 10")
        List<Map<String, Object>> countByCollegeTop10();

        /**
         * 统计本月活动数
         */
        @Select("SELECT COUNT(*) FROM activity WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE()) AND is_deleted = 0")
        int countMonthlyActivities();

        /**
         * 统计本月报名数
         */
        @Select("SELECT COUNT(*) FROM activity_registration WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE()) AND is_deleted = 0")
        int countMonthlyRegistrations();

        /**
         * 按月统计报名趋势（近6个月）
         */
        @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count " +
                        "FROM activity_registration " +
                        "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND is_deleted = 0 " +
                        "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
                        "ORDER BY month ASC")
        List<Map<String, Object>> getMonthlyTrend();

        /**
         * 统计组织者待审核人数
         */
        @Select("SELECT COUNT(*) FROM activity_registration r " +
                        "JOIN activity a ON r.activity_id = a.id " +
                        "WHERE a.organizer_id = #{orgId} AND r.status = 1 AND r.is_deleted = 0")
        int countOrgPendingAudits(Long orgId);

        /**
         * 统计组织者进行中活动数
         */
        @Select("SELECT COUNT(*) FROM activity WHERE organizer_id = #{orgId} AND status = 1 AND is_deleted = 0")
        int countOrgActiveActivities(Long orgId);

        /**
         * 统计组织者累计服务人次
         */
        @Select("SELECT COUNT(*) FROM activity_registration r " +
                        "JOIN activity a ON r.activity_id = a.id " +
                        "WHERE a.organizer_id = #{orgId} AND r.status = 2 AND r.is_deleted = 0")
        int countOrgTotalServiceCount(Long orgId);

        /**
         * 计算组织者综合评分
         */
        @Select("SELECT COALESCE(AVG(score), 0) FROM activity_review WHERE organizer_id = #{orgId} AND is_deleted = 0")
        double calcOrgAvgRating(Long orgId);

        /**
         * 获取组织者进行中的活动列表
         */
        @Select("SELECT id, title, " +
                        "(SELECT COUNT(*) FROM activity_registration WHERE activity_id = a.id AND is_deleted = 0) as current, "
                        +
                        "max_participants as total, '进行中' as status, 'primary' as statusType, " +
                        "DATE_FORMAT(create_time, '%Y-%m-%d') as createTime " +
                        "FROM activity a WHERE organizer_id = #{orgId} AND status IN (2, 3) AND is_deleted = 0 " +
                        "ORDER BY create_time DESC LIMIT 3")
        List<Map<String, Object>> selectOrgActiveActivities(Long orgId);

        /**
         * 获取组织者最新评价
         */
        @Select("SELECT r.id, COALESCE(u.nickname, v.name, '匿名用户') as nickname, u.avatar, r.score as rating, r.content, "
                        +
                        "DATE_FORMAT(r.create_time, '%Y-%m-%d %H:%i') as time " +
                        "FROM activity_review r " +
                        "LEFT JOIN volunteer v ON r.volunteer_id = v.id " +
                        "LEFT JOIN sys_user u ON v.user_id = u.id " +
                        "WHERE r.organizer_id = #{orgId} AND r.is_deleted = 0 " +
                        "ORDER BY r.create_time DESC LIMIT 3")
        List<Map<String, Object>> selectOrgLatestReviews(Long orgId);
}

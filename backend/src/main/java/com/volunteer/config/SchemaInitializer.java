package com.volunteer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SchemaInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database schema...");
        checkAndCreateOrganizerTable();
        checkAndAddCommentRatingColumn();
        migrateVideoUrls();
        fixCoverImageUrls();
        updateExpiredActivityDates();
        updateExpiredNoticeDates();
        syncParticipantCounts();
        log.info("Database schema check completed.");
    }

    /**
     * 同步 current_participants 与实际报名记录数，确保数据一致
     * 只统计有效报名（状态 0-3），不包含已取消(4)、缺席(5)、已拒绝(6)
     */
    private void syncParticipantCounts() {
        try {
            String checkTable = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'activity_registration' AND table_schema = DATABASE()";
            Integer tableExists = jdbcTemplate.queryForObject(checkTable, Integer.class);
            if (tableExists == null || tableExists == 0) {
                return;
            }

            // 用一条SQL将 current_participants 同步为实际有效报名数
            String syncSql = "UPDATE activity a SET a.current_participants = (" +
                    "SELECT COUNT(*) FROM activity_registration r " +
                    "WHERE r.activity_id = a.id AND r.is_deleted = 0 AND r.status IN (0, 1, 2, 3)" +
                    ") WHERE a.is_deleted = 0";
            int updated = jdbcTemplate.update(syncSql);
            log.info("同步报名人数完成: 更新了 {} 个活动的 current_participants", updated);

            // 修复 current_participants > max_participants 的异常数据（max_participants > 0 时）
            String fixOverflow = "UPDATE activity SET current_participants = max_participants " +
                    "WHERE max_participants > 0 AND current_participants > max_participants AND is_deleted = 0";
            int fixed = jdbcTemplate.update(fixOverflow);
            if (fixed > 0) {
                log.info("修复了 {} 个报名人数超限的活动", fixed);
            }

            // 清理超出 max_participants 的多余报名记录
            // 对每个超限活动，只保留 max_participants 条有效报名，其余软删除
            String findOverflow = "SELECT a.id, a.max_participants FROM activity a " +
                    "WHERE a.max_participants > 0 AND a.is_deleted = 0 AND " +
                    "(SELECT COUNT(*) FROM activity_registration r WHERE r.activity_id = a.id AND r.is_deleted = 0 AND r.status IN (0,1,2,3)) > a.max_participants";
            List<java.util.Map<String, Object>> overflowActivities = jdbcTemplate.queryForList(findOverflow);
            for (java.util.Map<String, Object> row : overflowActivities) {
                Long actId = ((Number) row.get("id")).longValue();
                Integer maxP = ((Number) row.get("max_participants")).intValue();
                // 软删除超出的报名记录（保留最早报名的 maxP 条）
                String cleanSql = "UPDATE activity_registration SET is_deleted = 1 " +
                        "WHERE activity_id = ? AND is_deleted = 0 AND status IN (0,1,2,3) AND id NOT IN " +
                        "(SELECT id FROM (SELECT id FROM activity_registration WHERE activity_id = ? AND is_deleted = 0 AND status IN (0,1,2,3) ORDER BY create_time ASC LIMIT ?) AS t)";
                int cleaned = jdbcTemplate.update(cleanSql, actId, actId, maxP);
                if (cleaned > 0) {
                    log.info("活动 {} 清理了 {} 条超限报名记录 (max={})", actId, cleaned, maxP);
                }
            }
        } catch (Exception e) {
            log.warn("同步报名人数失败: {}", e.getMessage());
        }
    }

    /**
     * 处理过期活动日期：
     * 保留一部分活动为"进行中"和"已结束"状态（真实场景需要这些状态），
     * 其余过期活动日期推到未来便于测试报名
     */
    private void updateExpiredActivityDates() {
        try {
            String checkTable = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'activity' AND table_schema = DATABASE()";
            Integer tableExists = jdbcTemplate.queryForObject(checkTable, Integer.class);
            if (tableExists == null || tableExists == 0) {
                log.info("Activity table does not exist yet, skipping date migration.");
                return;
            }

            // 先检查是否已有各状态的活动
            Integer ongoingCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM activity WHERE status = 3 AND is_deleted = 0", Integer.class);
            Integer endedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM activity WHERE status = 4 AND is_deleted = 0", Integer.class);

            // 如果已有进行中和已结束的活动，跳过整个过程
            if ((ongoingCount != null && ongoingCount > 0) && (endedCount != null && endedCount > 0)) {
                log.info("Already have ongoing({}) and ended({}) activities, skipping date fix.", ongoingCount, endedCount);
                return;
            }

            // 统计所有过期活动
            String countSql = "SELECT count(*) FROM activity WHERE end_time < NOW() AND is_deleted = 0";
            Integer expiredCount = jdbcTemplate.queryForObject(countSql, Integer.class);

            if (expiredCount == null || expiredCount == 0) {
                log.info("No expired activities found, skipping date migration.");
                return;
            }

            log.info("Found {} expired activities. Processing...", expiredCount);

            // 计算偏移量
            String offsetSql = "SELECT DATEDIFF(NOW(), MIN(start_time)) + 7 FROM activity WHERE end_time < NOW() AND is_deleted = 0";
            Integer offsetDays = jdbcTemplate.queryForObject(offsetSql, Integer.class);
            if (offsetDays == null || offsetDays <= 0) {
                return;
            }

            // 第一步：将所有过期活动日期推到未来
            String updateSql = "UPDATE activity SET " +
                    "start_time = DATE_ADD(start_time, INTERVAL ? DAY), " +
                    "end_time = DATE_ADD(end_time, INTERVAL ? DAY), " +
                    "register_start = CASE WHEN register_start IS NOT NULL THEN DATE_ADD(register_start, INTERVAL ? DAY) ELSE NULL END, " +
                    "register_end = CASE WHEN register_end IS NOT NULL THEN DATE_ADD(register_end, INTERVAL ? DAY) ELSE NULL END, " +
                    "deadline = CASE WHEN deadline IS NOT NULL THEN DATE_ADD(deadline, INTERVAL ? DAY) ELSE NULL END " +
                    "WHERE end_time < NOW() AND is_deleted = 0";
            int updated = jdbcTemplate.update(updateSql, offsetDays, offsetDays, offsetDays, offsetDays, offsetDays);
            log.info("Updated {} expired activities, shifted by {} days.", updated, offsetDays);

            // 第二步：状态重置（只重置那些日期被推到未来 且 原本是结束/取消状态的）
            String statusSql = "UPDATE activity SET status = 2, audit_status = 1 WHERE status IN (4, 5) AND end_time >= NOW() AND is_deleted = 0";
            int statusUpdated = jdbcTemplate.update(statusSql);
            log.info("Reset {} activities status to '已发布' for testing.", statusUpdated);

            // 第三步：从已发布活动中，选取一部分设为"进行中"和"已结束"以丰富演示数据
            // 取3个设为"进行中"（开始在过去、结束在未来）
            if (ongoingCount == null || ongoingCount == 0) {
                String ongoingSql = "UPDATE activity SET status = 3, " +
                        "start_time = DATE_SUB(NOW(), INTERVAL 2 DAY), " +
                        "end_time = DATE_ADD(NOW(), INTERVAL 5 DAY), " +
                        "register_start = DATE_SUB(NOW(), INTERVAL 10 DAY), " +
                        "register_end = DATE_SUB(NOW(), INTERVAL 3 DAY) " +
                        "WHERE status = 2 AND is_deleted = 0 ORDER BY id ASC LIMIT 3";
                int ongoingUpdated = jdbcTemplate.update(ongoingSql);
                log.info("Set {} activities to '进行中' (status=3)", ongoingUpdated);
            }

            // 取3个设为"已结束"（开始和结束都在过去）
            if (endedCount == null || endedCount == 0) {
                String endedSql = "UPDATE activity SET status = 4, " +
                        "start_time = DATE_SUB(NOW(), INTERVAL 15 DAY), " +
                        "end_time = DATE_SUB(NOW(), INTERVAL 3 DAY), " +
                        "register_start = DATE_SUB(NOW(), INTERVAL 25 DAY), " +
                        "register_end = DATE_SUB(NOW(), INTERVAL 16 DAY) " +
                        "WHERE status = 2 AND is_deleted = 0 ORDER BY id ASC LIMIT 3";
                int endedUpdated = jdbcTemplate.update(endedSql);
                log.info("Set {} activities to '已结束' (status=4)", endedUpdated);
            }

        } catch (Exception e) {
            log.error("Failed to update expired activity dates: {}", e.getMessage());
        }
    }

    /**
     * 自动将过期的公告日期推迟到未来
     */
    private void updateExpiredNoticeDates() {
        try {
            String checkTable = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'notice' AND table_schema = DATABASE()";
            Integer tableExists = jdbcTemplate.queryForObject(checkTable, Integer.class);
            if (tableExists == null || tableExists == 0) {
                return;
            }

            // 如果有过期公告，也推后
            String countSql = "SELECT count(*) FROM notice WHERE end_time IS NOT NULL AND end_time < NOW() AND is_deleted = 0";
            Integer expiredCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            if (expiredCount != null && expiredCount > 0) {
                String offsetSql = "SELECT DATEDIFF(NOW(), MIN(COALESCE(start_time, create_time))) + 7 FROM notice WHERE end_time IS NOT NULL AND end_time < NOW() AND is_deleted = 0";
                Integer offsetDays = jdbcTemplate.queryForObject(offsetSql, Integer.class);
                if (offsetDays != null && offsetDays > 0) {
                    String updateSql = "UPDATE notice SET " +
                            "start_time = CASE WHEN start_time IS NOT NULL THEN DATE_ADD(start_time, INTERVAL ? DAY) ELSE NULL END, " +
                            "end_time = CASE WHEN end_time IS NOT NULL THEN DATE_ADD(end_time, INTERVAL ? DAY) ELSE NULL END " +
                            "WHERE end_time IS NOT NULL AND end_time < NOW() AND is_deleted = 0";
                    int updated = jdbcTemplate.update(updateSql, offsetDays, offsetDays);
                    log.info("Updated {} expired notices, shifted by {} days.", updated, offsetDays);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update expired notice dates: {}", e.getMessage());
        }
    }


    /**
     * 修复课程封面图片URL中无效的后缀
     * 将 picsum.photos/seed/c1/400/300_4 修复为 picsum.photos/seed/c1_4/400/300
     */
    private void fixCoverImageUrls() {
        try {
            String checkSql = "SELECT count(*) FROM course WHERE cover_image LIKE '%/300_%' OR cover_image LIKE '%/400_%'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count != null && count > 0) {
                log.info("Found {} courses with invalid cover image URLs. Fixing...", count);
                // 修复 /300_N 后缀 (例如 /seed/c1/400/300_4 → /seed/c1_4/400/300)
                String fixSql = "UPDATE course SET cover_image = CONCAT(" +
                        "SUBSTRING_INDEX(cover_image, '/400/300_', 1), " +
                        "'_', SUBSTRING_INDEX(cover_image, '300_', -1), " +
                        "'/400/300') WHERE cover_image LIKE '%/300_%'";
                int updated = jdbcTemplate.update(fixSql);
                log.info("Fixed {} course cover image URLs.", updated);
            } else {
                log.info("All course cover image URLs are valid.");
            }
        } catch (Exception e) {
            log.error("Failed to fix cover image URLs: {}", e.getMessage());
        }
    }

    /**
     * 将 course 表中的 Google Cloud 视频链接替换为国内可访问的 CDN 链接
     */
    private void migrateVideoUrls() {
        try {
            String workingVideoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4";

            // 替换 Google Cloud 视频链接
            String checkGoogleSql = "SELECT count(*) FROM course WHERE video_url LIKE '%commondatastorage.googleapis.com%'";
            Integer googleCount = jdbcTemplate.queryForObject(checkGoogleSql, Integer.class);
            if (googleCount != null && googleCount > 0) {
                log.info("Found {} courses with Google Cloud video URLs. Migrating...", googleCount);
                String updateSql = "UPDATE course SET video_url = ? WHERE video_url LIKE '%commondatastorage.googleapis.com%'";
                int updated = jdbcTemplate.update(updateSql, workingVideoUrl);
                log.info("Migrated {} Google Cloud video URLs successfully.", updated);
            }

            // 替换已失效的头条 CDN 视频链接
            String checkToutiaoSql = "SELECT count(*) FROM course WHERE video_url LIKE '%toutiaostatic.com%'";
            Integer toutiaoCount = jdbcTemplate.queryForObject(checkToutiaoSql, Integer.class);
            if (toutiaoCount != null && toutiaoCount > 0) {
                log.info("Found {} courses with expired Toutiao CDN URLs. Migrating...", toutiaoCount);
                String updateSql = "UPDATE course SET video_url = ? WHERE video_url LIKE '%toutiaostatic.com%'";
                int updated = jdbcTemplate.update(updateSql, workingVideoUrl);
                log.info("Migrated {} Toutiao CDN video URLs successfully.", updated);
            }

            if ((googleCount == null || googleCount == 0) && (toutiaoCount == null || toutiaoCount == 0)) {
                log.info("All course video URLs are already using working sources.");
            }
        } catch (Exception e) {
            log.error("Failed to migrate video URLs: {}", e.getMessage());
        }
    }

    /**
     * 检查 comment 表是否缺少 rating 列，如果缺少则自动添加
     */
    private void checkAndAddCommentRatingColumn() {
        try {
            String checkSql = "SELECT count(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'comment' AND column_name = 'rating'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count != null && count == 0) {
                log.info("Column 'rating' not found in 'comment' table. Adding...");
                jdbcTemplate.execute(
                        "ALTER TABLE comment ADD COLUMN rating INT NULL DEFAULT NULL COMMENT '评分(1-5星)' AFTER content");
                log.info("Column 'rating' added to 'comment' table successfully.");
            } else {
                log.info("Column 'rating' already exists in 'comment' table.");
            }
        } catch (Exception e) {
            log.error("Failed to check/add 'rating' column to 'comment' table: {}", e.getMessage());
        }
    }

    private void checkAndCreateOrganizerTable() {
        try {
            // 检查表是否存在
            String checkSql = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'organizer'";
            // H2 uses different schema check, but assuming MySQL here based on driver class
            // in previous steps
            // If schema name is needed: table_schema = DATABASE()
            checkSql = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'organizer' AND table_schema = DATABASE()";

            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count != null && count == 0) {
                log.info("Table 'organizer' not found. Creating...");
                String createSql = "CREATE TABLE `organizer` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',\n" +
                        "  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',\n" +
                        "  `org_name` varchar(100) DEFAULT NULL COMMENT '组织名称',\n" +
                        "  `org_type` varchar(50) DEFAULT NULL COMMENT '组织类型',\n" +
                        "  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人姓名',\n" +
                        "  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',\n" +
                        "  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',\n" +
                        "  `description` text COMMENT '组织简介',\n" +
                        "  `logo` varchar(255) DEFAULT NULL COMMENT '组织Logo',\n" +
                        "  `address` varchar(255) DEFAULT NULL COMMENT '办公地址',\n" +
                        "  `verified` int(1) DEFAULT '0' COMMENT '是否认证：0-未认证，1-已认证',\n" +
                        "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                        "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
                        "  `is_deleted` int(1) DEFAULT '0' COMMENT '是否删除',\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `uk_user_id` (`user_id`)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织者信息表';";

                jdbcTemplate.execute(createSql);
                log.info("Table 'organizer' created successfully.");
            } else {
                log.info("Table 'organizer' already exists.");
            }
        } catch (Exception e) {
            log.error("Failed to check/create 'organizer' table: {}", e.getMessage());
        }
    }
}

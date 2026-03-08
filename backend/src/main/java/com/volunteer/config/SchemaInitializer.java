package com.volunteer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
        log.info("Database schema check completed.");
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

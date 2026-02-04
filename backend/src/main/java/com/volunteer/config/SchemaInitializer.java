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
        log.info("Database schema check completed.");
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

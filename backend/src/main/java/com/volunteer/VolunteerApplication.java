package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 校园志愿者管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.volunteer.mapper")
@EnableTransactionManagement
@EnableScheduling
public class VolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolunteerApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  校园志愿者管理系统启动成功!");
        System.out.println("  API文档: http://localhost:8080/api/doc.html");
        System.out.println("===========================================");
    }
}

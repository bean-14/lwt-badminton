package edu.scau.mis.lwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LWT课程预约系统启动类
 * 这是一个基于Spring Boot的运动场地课程预约管理系统
 * 包含学生、教练、管理员三种角色，支持课程预约、确认、取消等功能
 */
@SpringBootApplication
public class LwtServerApplication {

    /**
     * 应用程序入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(LwtServerApplication.class, args);
    }
}

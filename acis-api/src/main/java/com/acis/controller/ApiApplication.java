package com.acis.controller;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Neon Xie
 * @Date 2020/12/29
 * @description springboot启动类
 */
@ComponentScan(basePackages = {"com.acis"})
@MapperScan(basePackages = {"com.acis.dao"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}

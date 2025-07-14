package com.example.likeservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.example.likeservice", "com.example.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.likeservice.client"})
@MapperScan(basePackages = "com.example.likeservice.mapper", sqlSessionTemplateRef = "primarySqlSessionTemplate")
public class LikeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LikeServiceApplication.class, args);
    }

} 
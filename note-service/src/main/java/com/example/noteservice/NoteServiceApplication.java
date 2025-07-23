package com.example.noteservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.example.noteservice", "com.example.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.example.noteservice.client", "com.example.common.client"})
@MapperScan(basePackages = "com.example.noteservice.mapper", sqlSessionTemplateRef = "primarySqlSessionTemplate")
public class NoteServiceApplication {

    public static void main(String[] args) {
        // 强制设置路径匹配策略，解决 Springfox 兼容性问题
        System.setProperty("spring.mvc.pathmatch.matching-strategy", "ant_path_matcher");
        SpringApplication.run(NoteServiceApplication.class, args);
    }
}
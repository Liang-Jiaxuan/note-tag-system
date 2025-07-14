package com.example.likeservice.controller;

import com.example.common.response.BaseResponse;
import com.example.common.response.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 完全绕过Shiro认证
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class TestController {

    @GetMapping("/simple")
    @ApiOperation("简单测试接口")
    public BaseResponse<Map<String, Object>> simpleTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Like Service 简单测试接口正常工作");
        result.put("service", "like-service");
        result.put("port", 8083);
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("简单测试接口被调用");
        return ResultUtils.success(result);
    }

    @GetMapping("/health")
    @ApiOperation("健康检查接口")
    public BaseResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "like-service");
        result.put("port", 8083);
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("健康检查接口被调用");
        return ResultUtils.success(result);
    }
} 
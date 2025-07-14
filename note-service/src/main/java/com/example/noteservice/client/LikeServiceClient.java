package com.example.noteservice.client;

import com.example.common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "like-service")
public interface LikeServiceClient {

    /**
     * 测试like-service的发现接口
     */
    @GetMapping("/api/likes/test/discovery")
    BaseResponse<String> testLikeServiceDiscovery();
} 
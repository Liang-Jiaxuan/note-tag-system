package com.example.likeservice.client;

import com.example.common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Note Service Feign客户端
 * 用于like-service调用note-service的接口
 */
@FeignClient(name = "note-service", path = "/api/notes")
public interface NoteServiceClient {
    
    /**
     * 测试note-service服务发现
     */
    @GetMapping("/test/discovery")
    BaseResponse<String> testNoteServiceDiscovery();
} 
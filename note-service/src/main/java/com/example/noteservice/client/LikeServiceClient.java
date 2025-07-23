package com.example.noteservice.client;

import com.example.common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "like-service")
public interface LikeServiceClient {

    /**
     * 测试like-service的发现接口
     */
    @GetMapping("/api/likes/test/discovery")
    BaseResponse<String> testLikeServiceDiscovery();

    @GetMapping("/api/likes/popular/{limit}")
    BaseResponse<List<Long>> getPopularNoteIds(
            @PathVariable Integer limit,
            @RequestParam(defaultValue = "10") Integer minLikes,
            @RequestParam(defaultValue = "30") Integer days);

    @GetMapping("/api/likes/popular/page")
    BaseResponse<List<Long>> getPopularNoteIdsByPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(defaultValue = "5") Integer minLikes,
            @RequestParam(defaultValue = "30") Integer days);

    @GetMapping("/api/likes/popular/count")
    BaseResponse<Long> getPopularNotesCount(
            @RequestParam(defaultValue = "5") Integer minLikes,
            @RequestParam(defaultValue = "30") Integer days);
} 
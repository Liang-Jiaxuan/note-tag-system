package com.example.noteservice.controller;

import com.example.noteservice.service.AsyncProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/async")
public class AsyncProcessingController {
    
    @Resource
    private AsyncProcessingService asyncProcessingService;
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("processedCount", asyncProcessingService.getProcessedCount());
        statistics.put("errorCount", asyncProcessingService.getErrorCount());
        statistics.put("processedNotesCount", asyncProcessingService.getProcessedNotesCount());
        return statistics;
    }
    
    @PostMapping("/statistics/reset")
    public Map<String, String> resetStatistics() {
        asyncProcessingService.resetStatistics();
        Map<String, String> response = new HashMap<>();
        response.put("message", "统计信息已重置");
        return response;
    }
}
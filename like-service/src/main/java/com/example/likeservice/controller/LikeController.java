package com.example.likeservice.controller;

import com.example.common.annotation.RequiresPermission;
import com.example.common.enums.ErrorCode;
import com.example.common.response.BaseResponse;
import com.example.common.response.ResultUtils;
import com.example.likeservice.client.NoteServiceClient;
import com.example.likeservice.domain.dto.LikeRequest;
import com.example.likeservice.domain.dto.LikeResponse;
import com.example.likeservice.service.LikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/likes")
@Api(tags = "点赞管理")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    @Resource
    private NoteServiceClient noteServiceClient;
    
    @PostMapping("/toggle")
    @ApiOperation("点赞或取消点赞 - 请求体只需包含noteId，token从请求头Authorization获取")
    @RequiresPermission("like:toggle")
    public BaseResponse<LikeResponse> toggleLike(@Valid @RequestBody LikeRequest request, 
                                               HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取用户ID
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NO_AUTH, "用户未登录");
            }
            
            LikeResponse response = likeService.toggleLike(request, userId);
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞操作失败", e.getMessage());
        }
    }
    
    @GetMapping("/status/{noteId}")
    @ApiOperation("获取笔记点赞状态")
    @RequiresPermission("like:view")
    public BaseResponse<LikeResponse> getLikeStatus(@PathVariable Long noteId, 
                                                   HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NO_AUTH, "用户未登录");
            }
            
            LikeResponse response = likeService.getLikeStatus(noteId, userId);
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("获取点赞状态失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取点赞状态失败", e.getMessage());
        }
    }
    
    @GetMapping("/count/{noteId}")
    @ApiOperation("获取笔记点赞数量")
    public BaseResponse<Integer> getLikeCount(@PathVariable Long noteId) {
        try {
            Integer count = likeService.getLikeCount(noteId);
            return ResultUtils.success(count);
        } catch (Exception e) {
            log.error("获取点赞数量失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取点赞数量失败", e.getMessage());
        }
    }
    
    @GetMapping("/check/{noteId}")
    @ApiOperation("检查用户是否已点赞")
    @RequiresPermission("like:view")
    public BaseResponse<Boolean> isUserLiked(@PathVariable Long noteId, 
                                           HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NO_AUTH, "用户未登录");
            }
            
            Boolean isLiked = likeService.isUserLiked(noteId, userId);
            return ResultUtils.success(isLiked);
        } catch (Exception e) {
            log.error("检查点赞状态失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "检查点赞状态失败", e.getMessage());
        }
    }
    
    @GetMapping("/test/discovery")
    @ApiOperation("测试Eureka服务发现")
    public BaseResponse<String> testDiscovery() {
        try {
            log.info("=== Like Service 测试接口被调用 ===");
            String response = "Like Service 正常运行，服务名称: like-service，端口: 8083";
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("测试接口调用失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "测试接口调用失败", e.getMessage());
        }
    }
    
    @GetMapping("/test/feign/note-service")
    @ApiOperation("测试Feign远程调用note-service")
    public BaseResponse<String> testFeignNoteService() {
        try {
            log.info("=== Like Service 开始调用 Note Service ===");
            
            // 通过Feign调用note-service的测试接口
            BaseResponse<String> noteServiceResponse = noteServiceClient.testNoteServiceDiscovery();
            
            if (noteServiceResponse != null && noteServiceResponse.getData() != null) {
                String result = "Like Service 成功调用 Note Service！\n" +
                              "Like Service 信息: 服务名称: like-service，端口: 8083\n" +
                              "Note Service 响应: " + noteServiceResponse.getData();
                return ResultUtils.success(result);
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Note Service 响应为空");
            }
        } catch (Exception e) {
            log.error("Feign调用note-service失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Feign调用note-service失败: " + e.getMessage());
        }
    }
} 
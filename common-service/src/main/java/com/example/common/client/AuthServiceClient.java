package com.example.common.client;

import com.example.common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    /**
     * 验证权限
     */
    @PostMapping("/api/v1/permissions/validate")
    BaseResponse<Boolean> validatePermission(@RequestParam("permission") String permission, 
                                           @RequestHeader("Authorization") String token);

    /**
     * 根据Token获取用户权限
     */
    @PostMapping("/api/v1/permissions/user-permissions")
    BaseResponse<Map<String, Object>> getUserPermissionsByToken(@RequestHeader("Authorization") String token);
}
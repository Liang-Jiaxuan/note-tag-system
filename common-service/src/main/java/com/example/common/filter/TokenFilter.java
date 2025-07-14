package com.example.common.filter;

import com.example.common.auth.TokenAuthenticationToken;
import com.example.common.client.AuthServiceClient;
import com.example.common.context.TokenContext;
import com.example.common.response.BaseResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class TokenFilter extends AuthenticatingFilter implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private AuthServiceClient authServiceClient;

    public TokenFilter() {
        System.out.println("=== TokenFilter 构造函数被调用 ===");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.authServiceClient = applicationContext.getBean(AuthServiceClient.class);
        System.out.println("=== TokenFilter.setApplicationContext 被调用 ===");
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("=== TokenFilter.preHandle 被调用 ===");
        System.out.println("请求路径: " + ((HttpServletRequest) request).getRequestURI());
        System.out.println("请求方法: " + ((HttpServletRequest) request).getMethod());

        // 打印所有请求头
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        java.util.Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpRequest.getHeader(headerName);
            System.out.println("请求头: " + headerName + " = " + headerValue);
        }

        // 检查是否有token
        String token = getRequestToken(httpRequest);
        if (StringUtils.hasText(token)) {
            System.out.println("检测到token，执行认证");
            // 如果有token，执行认证
            boolean result = super.preHandle(request, response);
            System.out.println("preHandle 返回: " + result);
            return result;
        } else {
            System.out.println("没有token，允许继续执行");
            return true; // 没有token时允许继续执行
        }
    }

    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("=== TokenFilter.postHandle 被调用 ===");
        super.postHandle(request, response);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        System.out.println("=== TokenFilter.createToken 被调用 ===");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = getRequestToken(httpRequest);

        System.out.println("提取的 token: " + token);

        if (!StringUtils.hasText(token)) {
            System.out.println("Token 为空，返回 null");
            return null;
        }

        TokenAuthenticationToken authToken = new TokenAuthenticationToken(token);
        System.out.println("创建的认证 token: " + authToken.getToken());
        return authToken;
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, org.apache.shiro.subject.Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("=== TokenFilter.onLoginSuccess 被调用 ===");
        
        // 获取当前token
        String tokenValue = getRequestToken((HttpServletRequest) request);
        if (StringUtils.hasText(tokenValue)) {
            try {
                System.out.println("开始调用authServiceClient.getUserPermissionsByToken");
                // 通过Feign调用auth-service获取用户信息
                BaseResponse<Map<String, Object>> userResponse = authServiceClient.getUserPermissionsByToken("Bearer " + tokenValue);
                System.out.println("authServiceClient调用结果: " + userResponse);
                
                if (userResponse != null && userResponse.getData() != null) {
                    Map<String, Object> userInfo = userResponse.getData();
                    Object userIdObj = userInfo.get("userId");
                    System.out.println("获取到的userId: " + userIdObj);
                    
                    if (userIdObj != null) {
                        Long userId;
                        if (userIdObj instanceof Integer) {
                            userId = ((Integer) userIdObj).longValue();
                        } else if (userIdObj instanceof Long) {
                            userId = (Long) userIdObj;
                        } else {
                            userId = Long.valueOf(userIdObj.toString());
                        }
                        
                        // 设置userId到请求属性中
                        request.setAttribute("userId", userId);
                        System.out.println("设置userId到请求属性: " + userId);
                    } else {
                        System.out.println("userId为空");
                    }
                } else {
                    System.out.println("userResponse或userResponse.getData()为空");
                }
            } catch (Exception e) {
                System.out.println("获取用户信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return super.onLoginSuccess(token, subject, request, response);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("=== TokenFilter.onAccessDenied 被调用 ===");
        String token = getRequestToken((HttpServletRequest) request);

        System.out.println("Token: " + token);

        if (!StringUtils.hasText(token)) {
            System.out.println("没有 token，继续执行");
            return true;
        }

        System.out.println("执行登录验证");
        
        // 设置Token到上下文
        TokenContext.setToken("Bearer " + token);
        
        try {
            boolean result = executeLogin(request, response);
            System.out.println("executeLogin 返回: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("登录验证异常: " + e.getMessage());
            e.printStackTrace();
            return true; // 即使验证失败也继续执行
        } finally {
            // 清理Token上下文
            TokenContext.clear();
        }
    }

    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("请求头中的 Authorization: " + token);

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            String extractedToken = token.substring(7);
            System.out.println("提取的 token: " + extractedToken);
            return extractedToken;
        }
        return null;
    }
} 
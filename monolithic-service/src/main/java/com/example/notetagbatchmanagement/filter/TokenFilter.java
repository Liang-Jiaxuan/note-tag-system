package com.example.notetagbatchmanagement.filter;

import com.example.notetagbatchmanagement.auth.TokenAuthenticationToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class TokenFilter extends AuthenticatingFilter {

    public TokenFilter() {
        System.out.println("=== TokenFilter 构造函数被调用 ===");
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
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("=== TokenFilter.onAccessDenied 被调用 ===");
        String token = getRequestToken((HttpServletRequest) request);

        System.out.println("Token: " + token);

        if (!StringUtils.hasText(token)) {
            System.out.println("没有 token，继续执行");
            return true;
        }

        System.out.println("执行登录验证");
        try {
            boolean result = executeLogin(request, response);
            System.out.println("executeLogin 返回: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("登录验证异常: " + e.getMessage());
            e.printStackTrace();
            return true; // 即使验证失败也继续执行
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
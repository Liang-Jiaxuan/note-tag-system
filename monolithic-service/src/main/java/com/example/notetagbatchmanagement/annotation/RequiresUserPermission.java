package com.example.notetagbatchmanagement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresUserPermission {
    String value(); // 权限编码
    String targetParam() default ""; // 目标用户ID参数名
}
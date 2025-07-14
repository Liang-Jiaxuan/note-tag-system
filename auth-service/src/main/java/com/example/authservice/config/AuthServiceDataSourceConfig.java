package com.example.authservice.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class AuthServiceDataSourceConfig {

    // 认证数据源
    @Bean(name = "authDataSource")
    @Primary
    public DataSource authDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://127.0.0.1:3306/auth_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .username("root")
                .password("1617929300")
                .build();
    }

    // 认证数据源SqlSessionFactory
    @Bean(name = "authSqlSessionFactory")
    @Primary
    public SqlSessionFactory authSqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(authDataSource());
        // 检查mapper目录是否存在，如果不存在则不设置MapperLocations
        try {
            bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        } catch (Exception e) {
            // 如果mapper目录不存在，则不设置MapperLocations，使用注解方式
        }
        return bean.getObject();
    }

    // 认证数据源SqlSessionTemplate
    @Bean(name = "authSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate authSqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(authSqlSessionFactory());
    }
} 
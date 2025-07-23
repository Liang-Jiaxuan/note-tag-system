package com.example.noteservice.actuator;

import com.example.noteservice.mapper.NoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;  // 使用StringRedisTemplate

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            Integer dbCount = noteMapper.selectCount();
            log.debug("数据库健康检查通过，记录数: {}", dbCount);

            // 检查Redis连接 - 使用StringRedisTemplate避免序列化问题
            try {
                String redisKey = "health:test";
                stringRedisTemplate.opsForValue().set(redisKey, "test", 10);
                String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
                stringRedisTemplate.delete(redisKey);
                log.debug("Redis健康检查通过");

                return Health.up()
                        .withDetail("database", "MySQL 连接正常，记录数: " + dbCount)
                        .withDetail("redis", "Redis 连接正常，测试通过")
                        .withDetail("service", "note-service")
                        .withDetail("status", "所有组件运行正常")
                        .build();

            } catch (Exception redisException) {
                log.warn("Redis健康检查失败，但服务仍可运行: {}", redisException.getMessage());
                return Health.up()
                        .withDetail("database", "MySQL 连接正常，记录数: " + dbCount)
                        .withDetail("redis", "Redis 连接异常: " + redisException.getMessage())
                        .withDetail("service", "note-service")
                        .withDetail("status", "数据库正常，Redis异常")
                        .build();
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("service", "note-service")
                    .withDetail("status", "服务异常")
                    .build();
        }
    }
}
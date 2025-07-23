package com.example.noteservice.service.impl;

import com.example.noteservice.domain.dto.NoteDTO;
import com.example.noteservice.service.RedisCacheService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 添加随机数生成器
    private final Random random = new Random();

    // 缓存键前缀
    private static final String POPULAR_NOTES_KEY = "notes:popular:";
    private static final String NOTE_DETAIL_KEY = "note:detail:";
    private static final String NOTE_PAGE_KEY = "note:page:";
    
    // 缓存过期时间
    private static final long POPULAR_NOTES_EXPIRE = 300; // 5分钟
    private static final long NOTE_DETAIL_EXPIRE = 1800;  // 30分钟
    private static final long NOTE_PAGE_EXPIRE = 600;     // 10分钟

    @Override
    public List<NoteDTO> getPopularNotesFromCache(Integer limit, Integer minLikes, Integer days) {
        String key = POPULAR_NOTES_KEY + limit + ":" + minLikes + ":" + days;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            return null; // 缓存未命中，需要查询数据库
        }

        // 检查是否是空值标记
        if (result instanceof String && "EMPTY".equals(result)) {
            return new ArrayList<>(); // 返回空列表，防止穿透
        }

        return (List<NoteDTO>) result; // 返回正常数据
    }

    @Override
    public void setPopularNotesCache(Integer limit, Integer minLikes, Integer days, List<NoteDTO> notes) {
        String key = POPULAR_NOTES_KEY + limit + ":" + minLikes + ":" + days;

        if (notes == null || notes.isEmpty()) {
            // 缓存空值，设置随机过期时间防止雪崩
            long randomExpire = 60 + random.nextInt(60); // 60-120秒随机过期
            redisTemplate.opsForValue().set(key, "EMPTY", randomExpire, TimeUnit.SECONDS);
            log.info("缓存空值，随机过期时间: {}秒", randomExpire);
        } else {
            // 热门笔记缓存，设置随机过期时间防止雪崩
            long randomExpire = POPULAR_NOTES_EXPIRE + random.nextInt(300); // 5-10分钟随机过期
            redisTemplate.opsForValue().set(key, notes, randomExpire, TimeUnit.SECONDS);
            log.info("缓存热门笔记，随机过期时间: {}秒", randomExpire);
        }
    }

    @Override
    public IPage<NoteDTO> getPopularNotesPageFromCache(Long current, Long size, Integer minLikes, Integer days) {
        String key = POPULAR_NOTES_KEY + "page:" + current + ":" + size + ":" + minLikes + ":" + days;
        return (IPage<NoteDTO>) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setPopularNotesPageCache(Long current, Long size, Integer minLikes, Integer days, IPage<NoteDTO> page) {
        String key = POPULAR_NOTES_KEY + "page:" + current + ":" + size + ":" + minLikes + ":" + days;
        redisTemplate.opsForValue().set(key, page, POPULAR_NOTES_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public NoteDTO getNoteDetailFromCache(Long noteId) {
        String key = NOTE_DETAIL_KEY + noteId;
        return (NoteDTO) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setNoteDetailCache(Long noteId, NoteDTO note) {
        String key = NOTE_DETAIL_KEY + noteId;
        // 笔记详情缓存，设置随机过期时间
        long randomExpire = NOTE_DETAIL_EXPIRE + random.nextInt(600); // 30-40分钟随机过期
        redisTemplate.opsForValue().set(key, note, randomExpire, TimeUnit.SECONDS);
        log.info("缓存笔记详情，ID: {}, 随机过期时间: {}秒", noteId, randomExpire);
    }

    @Override
    public IPage<NoteDTO> getNotePageFromCache(Long current, Long size, String keyword) {
        String key = NOTE_PAGE_KEY + current + ":" + size + ":" + (keyword != null ? keyword.hashCode() : "null");
        return (IPage<NoteDTO>) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setNotePageCache(Long current, Long size, String keyword, IPage<NoteDTO> page) {
        String key = NOTE_PAGE_KEY + current + ":" + size + ":" + (keyword != null ? keyword.hashCode() : "null");
        // 分页缓存，设置随机过期时间
        long randomExpire = NOTE_PAGE_EXPIRE + random.nextInt(300); // 10-15分钟随机过期
        redisTemplate.opsForValue().set(key, page, randomExpire, TimeUnit.SECONDS);
        log.info("缓存分页数据，随机过期时间: {}秒", randomExpire);
    }

    @Override
    public void deleteNoteCache(Long noteId) {
        // 删除笔记详情缓存
        String detailKey = NOTE_DETAIL_KEY + noteId;
        redisTemplate.delete(detailKey);
        
        // 删除所有热门笔记缓存（因为可能包含该笔记）
        String popularPattern = POPULAR_NOTES_KEY + "*";
        redisTemplate.delete(redisTemplate.keys(popularPattern));
        
        // 删除所有分页缓存
        String pagePattern = NOTE_PAGE_KEY + "*";
        redisTemplate.delete(redisTemplate.keys(pagePattern));
    }

    @Override
    public void clearAllNoteCache() {
        String[] patterns = {
            POPULAR_NOTES_KEY + "*",
            NOTE_DETAIL_KEY + "*",
            NOTE_PAGE_KEY + "*"
        };
        
        for (String pattern : patterns) {
            redisTemplate.delete(redisTemplate.keys(pattern));
        }
    }
}
package com.example.noteservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.BaseResponse;
import com.example.common.client.AuthServiceClient;
import com.example.noteservice.actuator.CustomMetricsConfig;
import com.example.noteservice.client.LikeServiceClient;
import com.example.noteservice.domain.dto.CreateNoteDTO;
import com.example.noteservice.domain.dto.NoteDTO;
import com.example.noteservice.domain.event.NoteCreatedEvent;
import com.example.noteservice.domain.po.Note;
import com.example.noteservice.mapper.NoteMapper;
import com.example.noteservice.service.KafkaEventService;
import com.example.noteservice.service.NoteService;
import com.example.noteservice.service.RedisCacheService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Slf4j
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private AuthServiceClient authServiceClient;

    @Resource
    private LikeServiceClient likeServiceClient;

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CustomMetricsConfig customMetricsConfig;

    @Resource
    private KafkaEventService kafkaEventService;

    @Override
    @Transactional
    public NoteDTO createNote(CreateNoteDTO createNoteDTO) {
        Timer.Sample timer = customMetricsConfig.startNoteCreateTimer();
        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                customMetricsConfig.incrementNoteError();
                throw new BusinessException(ErrorCode.NULL_ERROR, "无法获取当前用户信息");
            }

            // 创建Note实体
            Note note = new Note();
            note.setTitle(createNoteDTO.getTitle());
            note.setContent(createNoteDTO.getContent());
            note.setCreatorId(currentUserId);
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());

            // 记录内容长度
            customMetricsConfig.recordNoteContentLength(createNoteDTO.getContent().length());
            customMetricsConfig.recordNoteTitleLength(createNoteDTO.getTitle().length());

            // 保存笔记
            save(note);

            // 发布笔记创建事件到Kafka
            try {
                NoteCreatedEvent event = new NoteCreatedEvent(
                        note.getId(),
                        currentUserId,
                        note.getTitle(),
                        note.getContent(),
                        note.getCreatedAt()
                );
                kafkaEventService.publishNoteCreatedEvent(event);
                log.info("笔记创建事件已发布: noteId={}, eventId={}", note.getId(), event.getEventId());
            } catch (Exception e) {
                log.error("发布笔记创建事件失败: noteId={}, error={}", note.getId(), e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            // 增加计数器
            customMetricsConfig.incrementNoteCreate();

            // 转换为DTO返回
            return convertToDTO(note);
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopNoteCreateTimer(timer);
        }
    }

    @Override
    public NoteDTO getNoteById(Long id) {
        Timer.Sample timer = customMetricsConfig.startNoteQueryTimer();
        try {
            // 1. 尝试从缓存获取
            NoteDTO cachedNote = redisCacheService.getNoteDetailFromCache(id);
            if (cachedNote != null) {
                log.info("从缓存获取笔记详情，ID: {}", id);
                customMetricsConfig.incrementNoteCacheHit();
                return cachedNote;
            }

            // 2. 缓存未命中，从数据库获取
            log.info("缓存未命中，从数据库获取笔记详情，ID: {}", id);
            customMetricsConfig.incrementNoteCacheMiss();

            Note note = noteMapper.selectById(id);
            if (note == null) {
                customMetricsConfig.incrementNoteError();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记不存在，ID: " + id);
            }

            if (note.getDeleted() == 1) {
                customMetricsConfig.incrementNoteError();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记已被删除，ID: " + id);
            }

            NoteDTO result = convertToDTO(note);

            // 3. 将结果存入缓存
            redisCacheService.setNoteDetailCache(id, result);
            log.info("笔记详情已存入缓存，ID: {}", id);

            customMetricsConfig.incrementNoteQuery();
            return result;
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopNoteQueryTimer(timer);
        }
    }

    @Override
    public List<NoteDTO> getAllNotes() {
        List<Note> notes = noteMapper.selectList(null);
        return notes.stream()
                .filter(note -> note.getDeleted() == 0)
                .map(note -> {
                    NoteDTO dto = new NoteDTO();
                    BeanUtils.copyProperties(note, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteDTO updateNote(Long id, NoteDTO noteDTO) {
        Timer.Sample timer = customMetricsConfig.startNoteUpdateTimer();
        try {
            Note existingNote = noteMapper.selectById(id);
            if (existingNote == null) {
                customMetricsConfig.incrementNoteError();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记不存在，ID: " + id);
            }

            if (existingNote.getDeleted() == 1) {
                customMetricsConfig.incrementNoteError();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记已被删除，ID: " + id);
            }

            existingNote.setTitle(noteDTO.getTitle());
            existingNote.setContent(noteDTO.getContent());
            existingNote.setUpdatedAt(LocalDateTime.now());
            noteMapper.updateById(existingNote);

            // 记录内容长度
            customMetricsConfig.recordNoteContentLength(noteDTO.getContent().length());
            customMetricsConfig.recordNoteTitleLength(noteDTO.getTitle().length());

            NoteDTO result = convertToDTO(existingNote);

            // 更新缓存
            redisCacheService.setNoteDetailCache(id, result);
            // 清除相关缓存
            redisCacheService.deleteNoteCache(id);

            customMetricsConfig.incrementNoteUpdate();
            return result;
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopNoteUpdateTimer(timer);
        }
    }

    @Override
    @Transactional
    public Boolean deleteNote(Long id) {
        Timer.Sample timer = customMetricsConfig.startNoteDeleteTimer();
        try {
            System.out.println("=== 开始删除笔记 ===");
            System.out.println("笔记ID: " + id);

            int result = noteMapper.deleteById(id);
            System.out.println("逻辑删除结果: " + result);

            if (result > 0) {
                // 删除相关缓存
                redisCacheService.deleteNoteCache(id);
                customMetricsConfig.incrementNoteDelete();
            } else {
                customMetricsConfig.incrementNoteError();
            }

            return result > 0;
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopNoteDeleteTimer(timer);
        }
    }

    /**
     *分页查询笔记
     * @param page 分页参数,包含当前页码,每页大小等信息
     * @param keyword 搜索关键词,用于搜索笔记时进行模糊匹配
     * @return 返回MyBatis-Plus的分页结果
     */
    @Override
    public IPage<NoteDTO> getNotesByPage(Page<Note> page, String keyword) {
        Timer.Sample timer = customMetricsConfig.startNoteQueryTimer();
        try {
            // 1. 尝试从缓存获取
            IPage<NoteDTO> cachedPage = redisCacheService.getNotePageFromCache(page.getCurrent(), page.getSize(), keyword);
            if (cachedPage != null) {
                log.info("从缓存获取笔记分页数据");
                customMetricsConfig.incrementNoteCacheHit();
                return cachedPage;
            }

            // 2. 缓存未命中，从数据库获取
            log.info("缓存未命中，从数据库获取笔记分页数据");
            customMetricsConfig.incrementNoteCacheMiss();

            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getContent, keyword);
            }
            wrapper.orderByDesc(Note::getCreatedAt);

            IPage<Note> notePage = this.page(page, wrapper);

            IPage<NoteDTO> dtoPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
            List<NoteDTO> dtoList = notePage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dtoPage.setRecords(dtoList);

            // 3. 将结果存入缓存
            redisCacheService.setNotePageCache(page.getCurrent(), page.getSize(), keyword, dtoPage);
            log.info("笔记分页数据已存入缓存");

            customMetricsConfig.incrementNoteQuery();
            return dtoPage;
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopNoteQueryTimer(timer);
        }
    }

    @Override
    public IPage<NoteDTO> getNotesByPage(Page<Note> page) {
        return getNotesByPage(page, null);
    }

    // getPopularNotes方法
    @Override
    public List<NoteDTO> getPopularNotes(Integer limit, Integer minLikes, Integer days) {
        Timer.Sample timer = customMetricsConfig.startPopularNotesQueryTimer();
        try {
            // 1. 尝试从缓存获取
            List<NoteDTO> cachedNotes = redisCacheService.getPopularNotesFromCache(limit, minLikes, days);
            if (cachedNotes != null) {
                log.info("从缓存获取热门笔记，数量: {}", cachedNotes.size());
                customMetricsConfig.incrementNoteCacheHit();
                return cachedNotes;
            }

            // 2. 缓存未命中，使用分布式锁防止击穿
            customMetricsConfig.incrementNoteCacheMiss();
            String lockKey = "lock:popular_notes:" + limit + ":" + minLikes + ":" + days;
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    // 双重检查：获取锁后再次检查缓存
                    cachedNotes = redisCacheService.getPopularNotesFromCache(limit, minLikes, days);
                    if (cachedNotes != null) {
                        log.info("双重检查：从缓存获取热门笔记，数量: {}", cachedNotes.size());
                        customMetricsConfig.incrementNoteCacheHit();
                        return cachedNotes;
                    }

                    // 3. 从数据库获取数据
                    log.info("缓存未命中，从数据库获取热门笔记");
                    List<Long> popularNoteIds = likeServiceClient.getPopularNoteIds(limit, minLikes, days).getData();

                    if (popularNoteIds.isEmpty()) {
                        redisCacheService.setPopularNotesCache(limit, minLikes, days, new ArrayList<>());
                        customMetricsConfig.incrementPopularNotesQuery();
                        return new ArrayList<>();
                    }

                    List<Note> popularNotes = this.list(new LambdaQueryWrapper<Note>()
                            .in(Note::getId, popularNoteIds)
                            .eq(Note::getDeleted, 0));

                    List<NoteDTO> noteDTOs = popularNotes.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

                    // 4. 将结果存入缓存
                    redisCacheService.setPopularNotesCache(limit, minLikes, days, noteDTOs);
                    log.info("热门笔记已存入缓存，数量: {}", noteDTOs.size());

                    customMetricsConfig.incrementPopularNotesQuery();
                    return noteDTOs;
                } finally {
                    // 5. 释放锁
                    redisTemplate.delete(lockKey);
                    log.info("释放分布式锁: {}", lockKey);
                }
            } else {
                // 6. 未获取到锁，等待其他线程更新缓存
                log.info("未获取到锁，等待其他线程更新缓存");
                try {
                    Thread.sleep(100); // 等待100ms
                    return getPopularNotes(limit, minLikes, days); // 递归重试
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("等待缓存更新时被中断", e);
                    customMetricsConfig.incrementNoteError();
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopPopularNotesQueryTimer(timer);
        }
    }

    // getPopularNotesByPage方法
    @Override
    public IPage<NoteDTO> getPopularNotesByPage(Page<Note> page, Integer minLikes, Integer days) {
        Timer.Sample timer = customMetricsConfig.startPopularNotesPageQueryTimer();
        try {
            // 1. 尝试从缓存获取
            IPage<NoteDTO> cachedPage = redisCacheService.getPopularNotesPageFromCache(
                    page.getCurrent(), page.getSize(), minLikes, days);
            if (cachedPage != null) {
                log.info("从缓存获取热门笔记分页数据");
                customMetricsConfig.incrementNoteCacheHit();
                return cachedPage;
            }

            // 2. 缓存未命中，从数据库获取
            log.info("缓存未命中，从数据库获取热门笔记分页数据");
            customMetricsConfig.incrementNoteCacheMiss();

            // 设置默认值
            if (minLikes == null) minLikes = 10;
            if (days == null) days = 30;

            try {
                // 获取热门笔记ID
                BaseResponse<List<Long>> idsResponse = likeServiceClient.getPopularNoteIdsByPage(
                        page.getCurrent(), page.getSize(), minLikes, days);

                if (idsResponse == null || idsResponse.getData() == null) {
                    customMetricsConfig.incrementNoteError();
                    return createEmptyPage(page);
                }

                List<Long> popularNoteIds = idsResponse.getData();

                if (popularNoteIds.isEmpty()) {
                    customMetricsConfig.incrementPopularNotesPageQuery();
                    return createEmptyPage(page);
                }

                // 查询笔记详情
                List<Note> popularNotes = this.list(new LambdaQueryWrapper<Note>()
                        .in(Note::getId, popularNoteIds)
                        .eq(Note::getDeleted, 0));

                List<NoteDTO> noteDTOs = popularNotes.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

                // 获取总数
                BaseResponse<Long> countResponse = likeServiceClient.getPopularNotesCount(minLikes, days);
                Long total = countResponse != null ? countResponse.getData() : 0L;

                IPage<NoteDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), total);
                resultPage.setRecords(noteDTOs);

                // 3. 将结果存入缓存
                redisCacheService.setPopularNotesPageCache(page.getCurrent(), page.getSize(), minLikes, days, resultPage);
                log.info("热门笔记分页数据已存入缓存");

                customMetricsConfig.incrementPopularNotesPageQuery();
                return resultPage;

            } catch (Exception e) {
                log.error("获取热门笔记分页数据失败", e);
                customMetricsConfig.incrementNoteError();
                return createEmptyPage(page);
            }
        } catch (Exception e) {
            customMetricsConfig.incrementNoteError();
            throw e;
        } finally {
            customMetricsConfig.stopPopularNotesPageQueryTimer(timer);
        }
    }
    // 通过Feign调用auth-service获取当前用户ID
    private Long getCurrentUserId() {
        try {
            // 获取当前请求的Token
            String token = getCurrentToken();
            if (token == null) {
                return null;
            }
            
            // 通过Feign调用auth-service获取用户信息
            BaseResponse<Map<String, Object>> response = authServiceClient.getUserPermissionsByToken("Bearer " + token);
            
            if (response != null && response.getData() != null) {
                Map<String, Object> userInfo = response.getData();
                Object userIdObj = userInfo.get("userId");
                if (userIdObj != null) {
                    if (userIdObj instanceof Integer) {
                        return ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        return (Long) userIdObj;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取当前请求的Token
    private String getCurrentToken() {
        try {
            // 从请求头中获取Authorization Token
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                javax.servlet.http.HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    return authorization.substring(7); // 去掉"Bearer "前缀
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // convertToDTO方法将带有逻辑删除字段的Note转换为不带逻辑删除字段的DTO
    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatorId(note.getCreatorId());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }

    private IPage<NoteDTO> createEmptyPage(Page<Note> page) {
        IPage<NoteDTO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
        emptyPage.setRecords(new ArrayList<>());
        return emptyPage;
    }
}

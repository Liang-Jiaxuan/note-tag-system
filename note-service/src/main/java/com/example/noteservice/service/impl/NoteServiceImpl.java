package com.example.noteservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.BaseResponse;
import com.example.common.client.AuthServiceClient;
import com.example.noteservice.domain.dto.CreateNoteDTO;
import com.example.noteservice.domain.dto.NoteDTO;
import com.example.noteservice.domain.po.Note;
import com.example.noteservice.mapper.NoteMapper;
import com.example.noteservice.service.NoteService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public NoteDTO createNote(CreateNoteDTO createNoteDTO) {
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "无法获取当前用户信息");
        }

        // 创建Note实体
        Note note = new Note();
        note.setTitle(createNoteDTO.getTitle());
        note.setContent(createNoteDTO.getContent());
        note.setCreatorId(currentUserId);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        // 保存笔记
        save(note);

        // 转换为DTO返回
        return convertToDTO(note);
    }

    @Override
    public NoteDTO getNoteById(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记不存在，ID: " + id);
        }

        if (note.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记已被删除，ID: " + id);
        }

        NoteDTO result = new NoteDTO();
        BeanUtils.copyProperties(note, result);
        return result;
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
        Note existingNote = noteMapper.selectById(id);
        if (existingNote == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记不存在，ID: " + id);
        }

        if (existingNote.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记已被删除，ID: " + id);
        }

        existingNote.setTitle(noteDTO.getTitle());
        existingNote.setContent(noteDTO.getContent());
        existingNote.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(existingNote);

        NoteDTO result = new NoteDTO();
        BeanUtils.copyProperties(existingNote, result);
        return result;
    }

    @Override
    @Transactional
    public Boolean deleteNote(Long id) {
        System.out.println("=== 开始删除笔记 ===");
        System.out.println("笔记ID: " + id);

        // 使用 MyBatis-Plus 的逻辑删除
        int result = noteMapper.deleteById(id);
        System.out.println("逻辑删除结果: " + result);

        return result > 0;
    }

    /**
     *分页查询笔记
     * @param page 分页参数,包含当前页码,每页大小等信息
     * @param keyword 搜索关键词,用于搜索笔记时进行模糊匹配
     * @return 返回MyBatis-Plus的分页结果
     */
    @Override
    public IPage<NoteDTO> getNotesByPage(Page<Note> page, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword);
        }
        wrapper.orderByDesc(Note::getCreatedAt);

        IPage<Note> notePage = this.page(page, wrapper);

        // 转换为 DTO，直接使用notePage的分页信息
        IPage<NoteDTO> dtoPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        List<NoteDTO> dtoList = notePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public IPage<NoteDTO> getNotesByPage(Page<Note> page) {
        return getNotesByPage(page, null);
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

    // 如果还没有 convertToDTO 方法，需要添加
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
}

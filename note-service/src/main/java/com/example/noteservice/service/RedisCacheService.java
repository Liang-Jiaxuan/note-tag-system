package com.example.noteservice.service;

import com.example.noteservice.domain.dto.NoteDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface RedisCacheService {
    
    List<NoteDTO> getPopularNotesFromCache(Integer limit, Integer minLikes, Integer days);
    
    void setPopularNotesCache(Integer limit, Integer minLikes, Integer days, List<NoteDTO> notes);
    
    IPage<NoteDTO> getPopularNotesPageFromCache(Long current, Long size, Integer minLikes, Integer days);
    
    void setPopularNotesPageCache(Long current, Long size, Integer minLikes, Integer days, IPage<NoteDTO> page);
    
    NoteDTO getNoteDetailFromCache(Long noteId);
    
    void setNoteDetailCache(Long noteId, NoteDTO note);
    
    IPage<NoteDTO> getNotePageFromCache(Long current, Long size, String keyword);
    
    void setNotePageCache(Long current, Long size, String keyword, IPage<NoteDTO> page);
    
    void deleteNoteCache(Long noteId);
    
    void clearAllNoteCache();
}
package com.example.noteservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.noteservice.domain.dto.CreateNoteDTO;
import com.example.noteservice.domain.dto.NoteDTO;
import com.example.noteservice.domain.po.Note;


import java.util.List;

public interface NoteService {
    NoteDTO createNote(CreateNoteDTO createNoteDTO);
    NoteDTO getNoteById(Long id);
    List<NoteDTO> getAllNotes();
    NoteDTO updateNote(Long id, NoteDTO noteDTO);
    Boolean deleteNote(Long id);
    IPage<NoteDTO> getNotesByPage(Page<Note> page, String keyword);
    IPage<NoteDTO> getNotesByPage(Page<Note> page);
    List<NoteDTO> getPopularNotes(Integer limit, Integer minLikes, Integer days);

    IPage<NoteDTO> getPopularNotesByPage(Page<Note> page, Integer minLikes, Integer days);
}

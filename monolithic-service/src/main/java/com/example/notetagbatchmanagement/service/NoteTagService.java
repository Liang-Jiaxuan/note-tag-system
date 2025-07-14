package com.example.notetagbatchmanagement.service;

import com.example.notetagbatchmanagement.domain.dto.CreateNoteTagDTO;
import com.example.notetagbatchmanagement.domain.dto.NoteTagDTO;
import com.example.notetagbatchmanagement.domain.po.NoteTag;

import java.util.List;

public interface NoteTagService {

    /**
     * 为多个笔记添加多个标签
     * @param noteIds 笔记ID列表
     * @param tagIds 标签ID列表
     */
    Boolean batchAddTagsToNotes(List<Long> noteIds, List<Long> tagIds);

    /**
     * 为多个笔记删除多个标签
     * @param noteIds 笔记ID列表
     * @param tagIds 标签ID列表
     */
    Boolean batchRemoveTagsFromNotes(List<Long> noteIds, List<Long> tagIds);


    // 新增的单个操作方法
    NoteTagDTO createNoteTag(CreateNoteTagDTO createNoteTagDTO);
    NoteTagDTO getNoteTagById(Long id);
    List<NoteTagDTO> getAllNoteTags();
    List<NoteTagDTO> getNoteTagsByNoteId(Long noteId);
    List<NoteTagDTO> getNoteTagsByTagId(Long tagId);
    NoteTagDTO updateNoteTag(Long id, NoteTagDTO noteTagDTO);
    Boolean deleteNoteTag(Long id);
}

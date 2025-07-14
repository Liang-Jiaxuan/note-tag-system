package com.example.notetagbatchmanagement.service;

import com.example.notetagbatchmanagement.domain.dto.CreateTagDTO;
import com.example.notetagbatchmanagement.domain.po.Tag;

import java.util.List;

import com.example.notetagbatchmanagement.domain.dto.TagDTO;
import java.util.List;

public interface TagService {
    TagDTO createTag(CreateTagDTO createTagDTO);
    TagDTO getTagById(Long id);
    List<TagDTO> getAllTags();
    TagDTO updateTag(Long id, TagDTO tagDTO);
    Boolean deleteTag(Long id);
}

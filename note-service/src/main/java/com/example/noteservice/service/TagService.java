package com.example.noteservice.service;

import com.example.noteservice.domain.dto.CreateTagDTO;
import com.example.noteservice.domain.dto.TagDTO;

import java.util.List;

public interface TagService {
    TagDTO createTag(CreateTagDTO createTagDTO);
    TagDTO getTagById(Long id);
    List<TagDTO> getAllTags();
    TagDTO updateTag(Long id, TagDTO tagDTO);
    Boolean deleteTag(Long id);
}

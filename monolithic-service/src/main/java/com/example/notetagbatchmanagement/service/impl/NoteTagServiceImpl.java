package com.example.notetagbatchmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.notetagbatchmanagement.common.ErrorCode;
import com.example.notetagbatchmanagement.domain.dto.CreateNoteTagDTO;
import com.example.notetagbatchmanagement.domain.dto.NoteTagDTO;
import com.example.notetagbatchmanagement.domain.po.Note;
import com.example.notetagbatchmanagement.domain.po.NoteTag;
import com.example.notetagbatchmanagement.domain.po.Tag;
import com.example.notetagbatchmanagement.exception.BusinessException;
import com.example.notetagbatchmanagement.mapper.NoteMapper;
import com.example.notetagbatchmanagement.mapper.NoteTagMapper;
import com.example.notetagbatchmanagement.mapper.TagMapper;
import com.example.notetagbatchmanagement.service.NoteTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteTagServiceImpl implements NoteTagService {

    @Resource
    private NoteTagMapper noteTagMapper;

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private TagMapper tagMapper;

    @Override
    @Transactional
    public Boolean batchAddTagsToNotes(List<Long> noteIds, List<Long> tagIds) {
        // 参数验证
        if (noteIds == null || noteIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "笔记ID列表不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "标签ID列表不能为空");
        }

        // 验证笔记ID是否存在
        validateNoteIds(noteIds);

        // 验证标签ID是否存在
        validateTagIds(tagIds);

        int totalAdded = 0;
        for (Long noteId : noteIds) {
            for (Long tagId : tagIds) {
                // 先检查是否存在已删除的关联
                NoteTag deletedNoteTag = noteTagMapper.selectByNoteIdAndTagIdAndDeleted(noteId, tagId, (short)1);

                if (deletedNoteTag != null) {
                    // 如果存在已删除的关联，则恢复它
                    System.out.println("发现已删除的关联，开始恢复: noteId=" + noteId + ", tagId=" + tagId);

                    int updateResult = noteTagMapper.updateDeletedById(
                            deletedNoteTag.getId(), (short)0, LocalDateTime.now());

                    if (updateResult > 0) {
                        totalAdded++;
                        System.out.println("恢复关联成功: noteId=" + noteId + ", tagId=" + tagId);
                    }
                } else {
                    // 检查是否存在未删除的关联
                    NoteTag existingNoteTag = noteTagMapper.selectByNoteIdAndTagIdAndDeleted(noteId, tagId, (short)0);

                    if (existingNoteTag == null) {
                        // 创建新关联
                        NoteTag noteTag = new NoteTag();
                        noteTag.setNoteId(noteId);
                        noteTag.setTagId(tagId);
                        noteTag.setCreatedAt(LocalDateTime.now());
                        noteTag.setUpdatedAt(LocalDateTime.now());
                        noteTag.setDeleted((short)0);

                        int insertResult = noteTagMapper.insert(noteTag);
                        if (insertResult > 0) {
                            totalAdded++;
                            System.out.println("创建新关联成功: noteId=" + noteId + ", tagId=" + tagId);
                        }
                    } else {
                        System.out.println("关联已存在，跳过: noteId=" + noteId + ", tagId=" + tagId);
                    }
                }
            }
        }

        System.out.println("批量添加完成，共添加 " + totalAdded + " 个关联");
        return true;
    }

    @Override
    @Transactional
    public Boolean batchRemoveTagsFromNotes(List<Long> noteIds, List<Long> tagIds) {
        // 参数验证
        if (noteIds == null || noteIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "笔记ID列表不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "标签ID列表不能为空");
        }

        // 验证笔记ID是否存在
        validateNoteIds(noteIds);

        // 验证标签ID是否存在
        validateTagIds(tagIds);

        // 使用 MyBatis-Plus 的逻辑删除
        int totalDeleted = 0;
        for (Long noteId : noteIds) {
            for (Long tagId : tagIds) {
                LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
                query.eq(NoteTag::getNoteId, noteId)
                        .eq(NoteTag::getTagId, tagId)
                        .eq(NoteTag::getDeleted, (short)0);

                NoteTag existingNoteTag = noteTagMapper.selectOne(query);
                if (existingNoteTag != null) {
                    int result = noteTagMapper.deleteById(existingNoteTag.getId());
                    if (result > 0) {
                        totalDeleted++;
                    }
                }
            }
        }

        System.out.println("批量移除完成，共移除 " + totalDeleted + " 个关联");
        return true;
    }

    /**
     * 验证笔记ID列表中的所有ID是否在数据库中存在
     */
    private void validateNoteIds(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "笔记ID列表不能为空");
        }

        // 检查是否有无效的ID（null或负数）
        for (Long noteId : noteIds) {
            if (noteId == null || noteId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记ID不能为空或无效值");
            }
        }

        // 查询数据库中实际存在的笔记ID
        LambdaQueryWrapper<Note> query = new LambdaQueryWrapper<>();
        query.in(Note::getId, noteIds)
                .eq(Note::getDeleted, (short)0);
        List<Note> existingNotes = noteMapper.selectList(query);

        // 检查是否所有ID都存在
        Set<Long> existingNoteIds = existingNotes.stream()
                .map(Note::getId)
                .collect(Collectors.toSet());

        List<Long> nonExistentNoteIds = noteIds.stream()
                .filter(id -> !existingNoteIds.contains(id))
                .collect(Collectors.toList());

        if (!nonExistentNoteIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "以下笔记ID不存在: " + nonExistentNoteIds);
        }
    }

    /**
     * 验证标签ID列表中的所有ID是否在数据库中存在
     */
    private void validateTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "标签ID列表不能为空");
        }

        // 检查是否有无效的ID（null或负数）
        for (Long tagId : tagIds) {
            if (tagId == null || tagId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签ID不能为空或无效值");
            }
        }

        // 查询数据库中实际存在的标签ID
        LambdaQueryWrapper<Tag> query = new LambdaQueryWrapper<>();
        query.in(Tag::getId, tagIds)
                .eq(Tag::getDeleted, (short)0);
        List<Tag> existingTags = tagMapper.selectList(query);

        // 检查是否所有ID都存在
        Set<Long> existingTagIds = existingTags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        List<Long> nonExistentTagIds = tagIds.stream()
                .filter(id -> !existingTagIds.contains(id))
                .collect(Collectors.toList());

        if (!nonExistentTagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "以下标签ID不存在: " + nonExistentTagIds);
        }
    }

    @Override
    @Transactional
    public NoteTagDTO createNoteTag(CreateNoteTagDTO createNoteTagDTO) {
        NoteTag noteTag = new NoteTag();
        BeanUtils.copyProperties(createNoteTagDTO, noteTag);
        noteTag.setCreatedAt(LocalDateTime.now());
        noteTag.setUpdatedAt(LocalDateTime.now());
        noteTag.setDeleted((short)0);
        noteTagMapper.insert(noteTag);

        NoteTagDTO result = new NoteTagDTO();
        BeanUtils.copyProperties(noteTag, result);
        return result;
    }

    @Override
    public NoteTagDTO getNoteTagById(Long id) {
        NoteTag noteTag = noteTagMapper.selectById(id);
        if (noteTag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记标签关联不存在");
        }

        if (noteTag.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记标签关联已被删除");
        }

        NoteTagDTO result = new NoteTagDTO();
        BeanUtils.copyProperties(noteTag, result);
        return result;
    }

    @Override
    public List<NoteTagDTO> getAllNoteTags() {
        List<NoteTag> noteTags = noteTagMapper.selectList(null);
        return noteTags.stream()
                .filter(noteTag -> noteTag.getDeleted() == 0)
                .map(noteTag -> {
                    NoteTagDTO dto = new NoteTagDTO();
                    BeanUtils.copyProperties(noteTag, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteTagDTO> getNoteTagsByNoteId(Long noteId) {
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getNoteId, noteId)
                .eq(NoteTag::getDeleted, (short)0);

        List<NoteTag> noteTags = noteTagMapper.selectList(query);
        return noteTags.stream()
                .map(noteTag -> {
                    NoteTagDTO dto = new NoteTagDTO();
                    BeanUtils.copyProperties(noteTag, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteTagDTO> getNoteTagsByTagId(Long tagId) {
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getTagId, tagId)
                .eq(NoteTag::getDeleted, (short)0);

        List<NoteTag> noteTags = noteTagMapper.selectList(query);
        return noteTags.stream()
                .map(noteTag -> {
                    NoteTagDTO dto = new NoteTagDTO();
                    BeanUtils.copyProperties(noteTag, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteTagDTO updateNoteTag(Long id, NoteTagDTO noteTagDTO) {
        NoteTag existingNoteTag = noteTagMapper.selectById(id);
        if (existingNoteTag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记标签关联不存在");
        }

        if (existingNoteTag.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "笔记标签关联已被删除");
        }

        existingNoteTag.setNoteId(noteTagDTO.getNoteId());
        existingNoteTag.setTagId(noteTagDTO.getTagId());
        existingNoteTag.setUpdatedAt(LocalDateTime.now());
        noteTagMapper.updateById(existingNoteTag);

        NoteTagDTO result = new NoteTagDTO();
        BeanUtils.copyProperties(existingNoteTag, result);
        return result;
    }

    @Override
    @Transactional
    public Boolean deleteNoteTag(Long id) {
        System.out.println("=== 开始删除笔记标签关联 ===");
        System.out.println("关联ID: " + id);

        // 使用 MyBatis-Plus 的逻辑删除
        int result = noteTagMapper.deleteById(id);
        System.out.println("逻辑删除结果: " + result);

        return result > 0;
    }
}

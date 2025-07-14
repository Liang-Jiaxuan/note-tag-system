package com.example.notetagbatchmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.notetagbatchmanagement.common.ErrorCode;
import com.example.notetagbatchmanagement.domain.dto.CreateTagDTO;
import com.example.notetagbatchmanagement.domain.dto.TagDTO;
import com.example.notetagbatchmanagement.domain.po.Tag;
import com.example.notetagbatchmanagement.exception.BusinessException;
import com.example.notetagbatchmanagement.mapper.TagMapper;
import com.example.notetagbatchmanagement.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    @Resource
    private TagMapper tagMapper;

    @Override
    @Transactional
    public TagDTO createTag(CreateTagDTO createTagDTO) {
        System.out.println("=== 开始创建标签 ===");
        System.out.println("标签名称: " + createTagDTO.getName());

        // 使用原生 SQL 查询已删除的同名标签
        Tag deletedTag = tagMapper.selectByNameAndDeleted(createTagDTO.getName(), (short)1);
        System.out.println("查询到的已删除标签: " + deletedTag);

        if (deletedTag != null) {
            System.out.println("发现已删除的同名标签，开始恢复...");

            // 使用原生 SQL 更新，绕过逻辑删除限制
            int updateResult = tagMapper.updateDeletedById(deletedTag.getId(), (short)0, LocalDateTime.now());
            System.out.println("恢复更新结果: " + updateResult);

            if (updateResult > 0) {
                // 验证恢复是否成功
                Tag restoredTag = tagMapper.selectById(deletedTag.getId());
                System.out.println("恢复后的标签: " + restoredTag);

                TagDTO result = new TagDTO();
                BeanUtils.copyProperties(restoredTag, result);
                return result;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复标签失败");
            }
        }

        System.out.println("没有找到已删除的同名标签，检查是否存在未删除的同名标签...");

        // 检查是否存在未删除的同名标签
        Tag existingTag = tagMapper.selectByNameAndDeleted(createTagDTO.getName(), (short)0);
        System.out.println("查询到的未删除标签: " + existingTag);

        if (existingTag != null) {
            System.out.println("发现未删除的同名标签，抛出异常");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在: " + createTagDTO.getName());
        }

        System.out.println("没有同名标签，开始创建新标签...");

        // 创建新标签
        Tag tag = new Tag();
        tag.setName(createTagDTO.getName());
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        tag.setDeleted((short)0);

        System.out.println("准备插入的标签: " + tag);
        int insertResult = tagMapper.insert(tag);
        System.out.println("插入结果: " + insertResult);

        TagDTO result = new TagDTO();
        BeanUtils.copyProperties(tag, result);
        return result;
    }

    @Override
    public TagDTO getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不存在，ID: " + id);
        }

        if (tag.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签已被删除，ID: " + id);
        }

        TagDTO result = new TagDTO();
        BeanUtils.copyProperties(tag, result);
        return result;
    }

    @Override
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagMapper.selectList(null);
        return tags.stream()
                .filter(tag -> tag.getDeleted() == 0)
                .map(tag -> {
                    TagDTO dto = new TagDTO();
                    BeanUtils.copyProperties(tag, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagDTO updateTag(Long id, TagDTO tagDTO) {
        Tag existingTag = tagMapper.selectById(id);
        if (existingTag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不存在，ID: " + id);
        }

        if (existingTag.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签已被删除，ID: " + id);
        }

        existingTag.setName(tagDTO.getName());
        existingTag.setUpdatedAt(LocalDateTime.now());
        tagMapper.updateById(existingTag);

        TagDTO result = new TagDTO();
        BeanUtils.copyProperties(existingTag, result);
        return result;
    }

    @Override
    @Transactional
    public Boolean deleteTag(Long id) {
        System.out.println("=== 开始删除标签 ===");
        System.out.println("标签ID: " + id);

        // 使用 MyBatis-Plus 的逻辑删除
        int result = tagMapper.deleteById(id);
        System.out.println("逻辑删除结果: " + result);

        return result > 0;
    }
}

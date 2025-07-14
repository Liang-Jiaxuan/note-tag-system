package com.example.noteservice.controller;

import com.example.common.annotation.RequiresPermission;
import com.example.common.response.BaseResponse;
import com.example.common.response.ResultUtils;
import com.example.noteservice.domain.dto.CreateTagDTO;
import com.example.noteservice.domain.dto.TagDTO;
import com.example.noteservice.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "标签管理")
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Resource
    private TagService tagService;

    @ApiOperation("创建标签")
    @PostMapping
    @RequiresPermission("tag:create")
    public BaseResponse<TagDTO> createTag(@Valid @RequestBody CreateTagDTO createTagDTO) {
        TagDTO result = tagService.createTag(createTagDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("根据ID获取标签")
    @GetMapping("/{id}")
    @RequiresPermission("tag:view")
    public BaseResponse<TagDTO> getTagById(@PathVariable Long id) {
        TagDTO result = tagService.getTagById(id);
        return ResultUtils.success(result);
    }

    @ApiOperation("获取所有标签")
    @GetMapping
    public BaseResponse<List<TagDTO>> getAllTags() {
        List<TagDTO> result = tagService.getAllTags();
        return ResultUtils.success(result);
    }

    @ApiOperation("更新标签")
    @PutMapping("/{id}")
    @RequiresPermission("tag:edit")
    public BaseResponse<TagDTO> updateTag(@PathVariable Long id, @Valid @RequestBody TagDTO tagDTO) {
        TagDTO result = tagService.updateTag(id, tagDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("逻辑删除标签")
    @PostMapping("/{id}/delete")
    @RequiresPermission("tag:delete")
    public BaseResponse<Boolean> deleteTag(@PathVariable Long id) {
        Boolean result = tagService.deleteTag(id);
        return ResultUtils.success(result);
    }
}
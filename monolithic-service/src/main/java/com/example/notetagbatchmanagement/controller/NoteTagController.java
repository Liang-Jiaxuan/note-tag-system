package com.example.notetagbatchmanagement.controller;

import com.example.notetagbatchmanagement.annotation.RequiresPermission;
import com.example.notetagbatchmanagement.common.BaseResponse;
import com.example.notetagbatchmanagement.common.ErrorCode;
import com.example.notetagbatchmanagement.common.ResultUtils;
import com.example.notetagbatchmanagement.domain.dto.BatchNotesTagsRequest;
import com.example.notetagbatchmanagement.domain.dto.CreateNoteTagDTO;
import com.example.notetagbatchmanagement.domain.dto.NoteTagDTO;
import com.example.notetagbatchmanagement.service.NoteTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "笔记标签关联管理")
@RestController
@RequestMapping("/api/notes-tags")
public class NoteTagController {

    @Resource
    private NoteTagService noteTagService;

    @ApiOperation("批量为笔记添加标签")
    @PostMapping ("/batch-add")
    @RequiresPermission("notetag:batch")
    public BaseResponse<Boolean> batchAddTagsToNotes(@RequestBody BatchNotesTagsRequest request){
            Boolean result = noteTagService.batchAddTagsToNotes(request.getNoteIds(), request.getTagIds());
            return ResultUtils.success(result);
    }

    @ApiOperation("批量移除笔记的标签")
    @PostMapping("/batch-remove")
    @RequiresPermission("notetag:batch")
    public BaseResponse<Boolean> batchRemoveTagsFromNotes(@RequestBody BatchNotesTagsRequest request){
            Boolean result = noteTagService.batchRemoveTagsFromNotes(request.getNoteIds(),request.getTagIds());
            return ResultUtils.success(result);
    }

    @ApiOperation("创建笔记标签关联")
    @PostMapping
    @RequiresPermission("notetag:create")
    public BaseResponse<NoteTagDTO> createNoteTag(@Valid @RequestBody CreateNoteTagDTO createNoteTagDTO) {
        NoteTagDTO result = noteTagService.createNoteTag(createNoteTagDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("根据ID获取笔记标签关联")
    @GetMapping("/{id}")
    @RequiresPermission("notetag:view")
    public BaseResponse<NoteTagDTO> getNoteTagById(@PathVariable Long id) {
        NoteTagDTO result = noteTagService.getNoteTagById(id);
        return ResultUtils.success(result);
    }

    @ApiOperation("获取所有笔记标签关联")
    @GetMapping
    @RequiresPermission("notetag:view")
    public BaseResponse<List<NoteTagDTO>> getAllNoteTags() {
        List<NoteTagDTO> result = noteTagService.getAllNoteTags();
        return ResultUtils.success(result);
    }

    @ApiOperation("根据笔记ID获取关联")
    @GetMapping("/note/{noteId}")
    @RequiresPermission("notetag:view")
    public BaseResponse<List<NoteTagDTO>> getNoteTagsByNoteId(@PathVariable Long noteId) {
        List<NoteTagDTO> result = noteTagService.getNoteTagsByNoteId(noteId);
        return ResultUtils.success(result);
    }

    @ApiOperation("根据标签ID获取关联")
    @GetMapping("/tag/{tagId}")
    @RequiresPermission("notetag:view")
    public BaseResponse<List<NoteTagDTO>> getNoteTagsByTagId(@PathVariable Long tagId) {
        List<NoteTagDTO> result = noteTagService.getNoteTagsByTagId(tagId);
        return ResultUtils.success(result);
    }

    @ApiOperation("更新笔记标签关联")
    @PutMapping("/{id}")
    @RequiresPermission("notetag:edit")
    public BaseResponse<NoteTagDTO> updateNoteTag(@PathVariable Long id, @Valid @RequestBody NoteTagDTO noteTagDTO) {
        NoteTagDTO result = noteTagService.updateNoteTag(id, noteTagDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("逻辑删除笔记标签关联")
    @PostMapping("/{id}/delete")
    @RequiresPermission("notetag:delete")
    public BaseResponse<Boolean> deleteNoteTag(@PathVariable Long id) {
        Boolean result = noteTagService.deleteNoteTag(id);
        return ResultUtils.success(result);
    }
}

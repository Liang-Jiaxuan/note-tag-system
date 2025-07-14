package com.example.notetagbatchmanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.notetagbatchmanagement.annotation.RequiresPermission;
import com.example.notetagbatchmanagement.common.BaseResponse;
import com.example.notetagbatchmanagement.common.ErrorCode;
import com.example.notetagbatchmanagement.common.ResultUtils;
import com.example.notetagbatchmanagement.domain.dto.CreateNoteDTO;
import com.example.notetagbatchmanagement.domain.dto.NoteDTO;
import com.example.notetagbatchmanagement.domain.po.Note;
import com.example.notetagbatchmanagement.domain.po.User;
import com.example.notetagbatchmanagement.exception.BusinessException;
import com.example.notetagbatchmanagement.service.NoteService;
import com.example.notetagbatchmanagement.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "笔记管理")
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Resource
    private NoteService noteService;

    @Resource
    private UserService userService;

    @ApiOperation("创建笔记")
    @PostMapping
    @RequiresPermission("note:create")
    public BaseResponse<NoteDTO> createNote(@Valid @RequestBody CreateNoteDTO createNoteDTO) {
        NoteDTO result = noteService.createNote(createNoteDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("根据ID获取笔记")
    @GetMapping("/{id}")
    @RequiresPermission("note:view:public")
    public BaseResponse<NoteDTO> getNoteById(@PathVariable Long id) {
        NoteDTO result = noteService.getNoteById(id);
        return ResultUtils.success(result);
    }

    @ApiOperation("获取所有笔记")
    @GetMapping
//    @RequiresPermission("note:view:public")
    public BaseResponse<List<NoteDTO>> getAllNotes() {
        List<NoteDTO> result = noteService.getAllNotes();
        return ResultUtils.success(result);
    }

    @ApiOperation("更新任意笔记")
    @PutMapping("/admin/{id}")
    @RequiresPermission("note:edit:all")
    public BaseResponse<NoteDTO> updateAnyNote(@PathVariable Long id, @Valid @RequestBody NoteDTO noteDTO) {
        NoteDTO result = noteService.updateNote(id, noteDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("更新个人笔记")
    @PutMapping("/{id}")
    @RequiresPermission("note:edit:own")
    public BaseResponse<NoteDTO> updateMyNote(@PathVariable Long id, @Valid @RequestBody NoteDTO noteDTO) {
        // 验证是否为笔记创建者
        validateNoteOwner(id);
        NoteDTO result = noteService.updateNote(id, noteDTO);
        return ResultUtils.success(result);
    }

    @ApiOperation("逻辑删除-任意笔记")
    @PostMapping("/admin/{id}/delete")
    @RequiresPermission("note:delete:all")
    public BaseResponse<Boolean> deleteAnyNote(@PathVariable Long id) {
        Boolean result = noteService.deleteNote(id);
        return ResultUtils.success(result);
    }

    @ApiOperation("逻辑删除-个人笔记")
    @DeleteMapping("/{id}")
    @RequiresPermission("note:delete:own")
    public BaseResponse<Boolean> deleteMyNote(@PathVariable Long id) {
        // 验证是否为笔记创建者
        validateNoteOwner(id);
        Boolean result = noteService.deleteNote(id);
        return ResultUtils.success(result);
    }

    @ApiOperation("分页查询笔记")
    @GetMapping("/page")
//    @RequiresPermission("note:view:public")
    public BaseResponse<IPage<NoteDTO>> getNotesByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Note> page = new Page<>(current, size);
        IPage<NoteDTO> result = noteService.getNotesByPage(page, keyword);
        return ResultUtils.success(result);
    }

    @ApiOperation("分页查询所有笔记")
    @GetMapping("/page/all")
//    @RequiresPermission("note:view:public")
    public BaseResponse<IPage<NoteDTO>> getAllNotesByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Note> page = new Page<>(current, size);
        IPage<NoteDTO> result = noteService.getNotesByPage(page);
        return ResultUtils.success(result);
    }

    // 验证是否为笔记创建者
    private void validateNoteOwner(Long noteId) {
        try {
            // 从 Shiro 中获取当前用户信息
            Subject subject = SecurityUtils.getSubject();
            String username = (String) subject.getPrincipal();

            if (username == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "用户未登录");
            }

            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
            }

            // 获取笔记DTO信息
            NoteDTO noteDTO = noteService.getNoteById(noteId);
            if (noteDTO == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "笔记不存在");
            }

            // 验证是否为笔记创建者（假设NoteDTO有creatorId字段）
            if (!noteDTO.getCreatorId().equals(currentUser.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH, "您没有权限操作此笔记");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证权限时发生错误");
        }
    }
}
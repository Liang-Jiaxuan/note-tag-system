package com.example.noteservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.RequiresPermission;
import com.example.common.enums.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.BaseResponse;
import com.example.common.response.ResultUtils;
import com.example.common.client.AuthServiceClient;
import com.example.noteservice.client.LikeServiceClient;
import com.example.noteservice.domain.dto.CreateNoteDTO;
import com.example.noteservice.domain.dto.NoteDTO;
import com.example.noteservice.domain.po.Note;
import com.example.noteservice.service.NoteService;
import com.example.noteservice.service.RedisCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "笔记管理")
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Resource
    private NoteService noteService;

    @Resource
    private AuthServiceClient authServiceClient;
    
    @Resource
    private LikeServiceClient likeServiceClient;

    @Resource
    private RedisCacheService redisCacheService;

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
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Note> page = new Page<>(current, size);
        IPage<NoteDTO> result = noteService.getNotesByPage(page, keyword);
        return ResultUtils.success(result);
    }

    @ApiOperation("分页查询所有笔记")
    @GetMapping("/page/all")
//    @RequiresPermission("note:view:public")
    public BaseResponse<IPage<NoteDTO>> getAllNotesByPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Note> page = new Page<>(current, size);
        IPage<NoteDTO> result = noteService.getNotesByPage(page);
        return ResultUtils.success(result);
    }

    @ApiOperation("测试Eureka服务发现")
    @GetMapping("/test/discovery")
    public BaseResponse<String> testDiscovery() {
        try {
            log.info("=== Note Service 测试接口被调用 ===");
            String response = "Note Service 正常运行，服务名称: note-service，端口: 8082";
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("测试接口调用失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "测试接口调用失败", e.getMessage());
        }
    }
    
    @ApiOperation("测试Feign远程调用like-service")
    @GetMapping("/test/feign/like-service")
    public BaseResponse<String> testFeignLikeService() {
        try {
            log.info("=== Note Service 开始调用 Like Service ===");
            
            // 通过Feign调用like-service的测试接口
            BaseResponse<String> likeServiceResponse = likeServiceClient.testLikeServiceDiscovery();
            
            if (likeServiceResponse != null && likeServiceResponse.getData() != null) {
                String result = "Note Service 成功调用 Like Service！\n" +
                              "Note Service 信息: 服务名称: note-service，端口: 8082\n" +
                              "Like Service 响应: " + likeServiceResponse.getData();
                return ResultUtils.success(result);
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Like Service 响应为空");
            }
        } catch (Exception e) {
            log.error("Feign调用like-service失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Feign调用like-service失败: " + e.getMessage());
        }
    }

    /**
     * 该接口无法查询点赞数为0的笔记,
     * 因为在数据库中只有点赞后才会生成点赞记录,进而进行点赞统计
     * 而如果某个笔记没有被点过赞, 那就不会出现在点赞统计的表中
     * 该接口最终就是在点赞统计表中查询笔记id的
     * @param limit 指定要获取多少个满足条件(点赞数和最近几天)的笔记
     * @param minLikes 指定多少赞
     * @param days 指定最近多少天
     * @return
     */
    @ApiOperation("获取热门笔记列表")
    @GetMapping("/popular")
    public BaseResponse<List<NoteDTO>> getPopularNotes(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "5") Integer minLikes,
            @RequestParam(defaultValue = "30") Integer days) {

        List<NoteDTO> popularNotes = noteService.getPopularNotes(limit, minLikes, days);
        return ResultUtils.success(popularNotes);
    }

    /**
     * 该接口无法查询点赞数为0的笔记,
     * 因为在数据库中只有点赞后才会生成点赞记录,进而进行点赞统计
     * 而如果某个笔记没有被点过赞, 那就不会出现在点赞统计的表中
     * 该接口最终就是在点赞统计表中查询笔记id的
     * @param current
     * @param size
     * @param minLikes
     * @param days
     * @return
     */
    @ApiOperation("分页查询热门笔记")
    @GetMapping("/popular/page")
    public BaseResponse<IPage<NoteDTO>> getPopularNotesByPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(defaultValue = "5") Integer minLikes,
            @RequestParam(defaultValue = "30") Integer days) {

        Page<Note> page = new Page<>(current, size);
        IPage<NoteDTO> result = noteService.getPopularNotesByPage(page, minLikes, days);
        return ResultUtils.success(result);
    }

    @ApiOperation("清除所有笔记缓存")
    @PostMapping("/cache/clear")
    @RequiresPermission("note:manage")
    public BaseResponse<String> clearAllCache() {
        redisCacheService.clearAllNoteCache();
        return ResultUtils.success("所有笔记缓存已清除");
    }

    @ApiOperation("清除指定笔记缓存")
    @PostMapping("/cache/clear/{noteId}")
    @RequiresPermission("note:manage")
    public BaseResponse<String> clearNoteCache(@PathVariable Long noteId) {
        redisCacheService.deleteNoteCache(noteId);
        return ResultUtils.success("笔记缓存已清除，ID: " + noteId);
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

            // 通过Feign调用auth-service获取用户信息
            Long currentUserId = getCurrentUserId(username);
            if (currentUserId == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
            }

            // 获取笔记DTO信息
            NoteDTO noteDTO = noteService.getNoteById(noteId);
            if (noteDTO == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "笔记不存在");
            }

            // 验证是否为笔记创建者（假设NoteDTO有creatorId字段）
            if (!noteDTO.getCreatorId().equals(currentUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "您没有权限操作此笔记");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证权限时发生错误");
        }
    }

    // 通过Feign调用auth-service获取用户ID
    private Long getCurrentUserId(String username) {
        try {
            // 获取当前请求的Token
            String token = getCurrentToken();
            
            // 通过Feign调用auth-service获取用户信息
            BaseResponse<Map<String, Object>> response = authServiceClient.getUserPermissionsByToken(token);
            
            if (response != null && response.getData() != null) {
                Map<String, Object> userInfo = response.getData();
                Object userIdObj = userInfo.get("userId");
                if (userIdObj != null) {
                    if (userIdObj instanceof Integer) {
                        return ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        return (Long) userIdObj;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取当前请求的Token
    private String getCurrentToken() {
        try {
            // 从请求头中获取Authorization Token
            javax.servlet.http.HttpServletRequest request = 
                ((org.springframework.web.context.request.ServletRequestAttributes) 
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
            
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7); // 去掉"Bearer "前缀
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
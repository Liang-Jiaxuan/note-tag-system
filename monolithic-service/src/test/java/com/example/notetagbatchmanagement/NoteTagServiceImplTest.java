package com.example.notetagbatchmanagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.notetagbatchmanagement.domain.po.NoteTag;
import com.example.notetagbatchmanagement.exception.BusinessException;
import com.example.notetagbatchmanagement.mapper.NoteTagMapper;
import com.example.notetagbatchmanagement.service.NoteTagService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NoteTagServiceImplTest {
    @Resource
    private NoteTagService noteTagService;

    @Resource
    private NoteTagMapper noteTagMapper;

    @BeforeAll
    static void setUp() {
        // 为所有测试方法设置环境变量
        System.setProperty("example.db.host", "127.0.0.1");
        System.setProperty("example.db.dbname", "second_intern");
        System.setProperty("example.db.pw", "1617929300");
    }

    @Test
    @DisplayName("正常场景：批量添加标签")
    void testBatchAddTagsToNotes_Success() {
        // Given
        List<Long> noteIds = Arrays.asList(2L, 3L);
        List<Long> tagIds = Arrays.asList(1L, 2L);

        // When
        noteTagService.batchAddTagsToNotes(noteIds, tagIds);

        // Then
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getDeleted, (short)0);
        List<NoteTag> result = noteTagMapper.selectList(query);
        assertEquals(4, result.size()); // 2个笔记 × 2个标签 = 4条记录
    }

    @Test
    @DisplayName("空列表场景：批量添加标签")
    void testBatchAddTagsToNotes_EmptyLists() {
        // Given
        List<Long> noteIds = new ArrayList<>();
        List<Long> tagIds = Arrays.asList(1L, 2L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchAddTagsToNotes(noteIds, tagIds));

        assertEquals("笔记ID列表不能为空", exception.getDescription());
    }

    @Test
    @DisplayName("空值场景：批量添加标签")
    void testBatchAddTagsToNotes_NullLists() {
        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchAddTagsToNotes(null, null));

        assertEquals("笔记ID列表不能为空", exception.getDescription());
    }

    @Test
    @DisplayName("无效ID场景：批量添加标签")
    void testBatchAddTagsToNotes_InvalidIds() {
        // Given
        List<Long> noteIds = Arrays.asList(-1L, 0L);
        List<Long> tagIds = Arrays.asList(1L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchAddTagsToNotes(noteIds, tagIds));

        assertTrue(exception.getDescription().contains("笔记ID不能为空或无效值"));
    }

    @Test
    @DisplayName("不存在的笔记ID：批量添加标签")
    void testBatchAddTagsToNotes_NonExistentNoteIds() {
        // Given
        List<Long> noteIds = Arrays.asList(999L, 1000L);
        List<Long> tagIds = Arrays.asList(1L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchAddTagsToNotes(noteIds, tagIds));

        assertTrue(exception.getDescription().contains("以下笔记ID不存在"));
    }

    @Test
    @DisplayName("不存在的标签ID：批量添加标签")
    void testBatchAddTagsToNotes_NonExistentTagIds() {
        // Given
        List<Long> noteIds = Arrays.asList(2L);
        List<Long> tagIds = Arrays.asList(999L, 1000L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchAddTagsToNotes(noteIds, tagIds));

        assertTrue(exception.getDescription().contains("以下标签ID不存在"));
    }

    @Test
    @DisplayName("空列表场景：批量移除标签")
    void testBatchRemoveTagsFromNotes_EmptyLists() {
        // Given
        List<Long> noteIds = new ArrayList<>();
        List<Long> tagIds = Arrays.asList(1L, 2L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchRemoveTagsFromNotes(noteIds, tagIds));

        assertEquals("笔记ID列表不能为空", exception.getDescription());
    }

    @Test
    @DisplayName("空值场景：批量移除标签")
    void testBatchRemoveTagsFromNotes_NullLists() {
        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchRemoveTagsFromNotes(null, null));

        assertEquals("笔记ID列表不能为空", exception.getDescription());
    }

    @Test
    @DisplayName("无效ID场景：批量移除标签")
    void testBatchRemoveTagsFromNotes_InvalidIds() {
        // Given
        List<Long> noteIds = Arrays.asList(-1L, 0L);
        List<Long> tagIds = Arrays.asList(1L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchRemoveTagsFromNotes(noteIds, tagIds));

        assertTrue(exception.getDescription().contains("笔记ID不能为空或无效值"));
    }

    @Test
    @DisplayName("移除不存在的关联：应该正常处理")
    void testBatchRemoveTagsFromNotes_NonExistent() {
        // Given
        List<Long> noteIds = Arrays.asList(999L);
        List<Long> tagIds = Arrays.asList(999L);

        // When & Then - 验证抛出BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () ->
                noteTagService.batchRemoveTagsFromNotes(noteIds, tagIds));

        assertTrue(exception.getDescription().contains("以下笔记ID不存在"));
    }

    @Test
    @DisplayName("重复添加场景：不应创建重复记录")
    void testBatchAddTagsToNotes_Duplicate() {
        // Given
        List<Long> noteIds = Arrays.asList(2L);
        List<Long> tagIds = Arrays.asList(1L);

        // When - 第一次添加
        noteTagService.batchAddTagsToNotes(noteIds, tagIds);

        // When - 第二次添加相同数据
        noteTagService.batchAddTagsToNotes(noteIds, tagIds);

        // Then - 应该只有一条记录
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getNoteId, 2L)
                .eq(NoteTag::getTagId, 1L)
                .eq(NoteTag::getDeleted, (short)0);
        long count = noteTagMapper.selectCount(query);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("正常场景：批量移除标签")
    void testBatchRemoveTagsFromNotes_Success() {
        // Given - 先添加一些标签
        List<Long> noteIds = Arrays.asList(2L, 3L);
        List<Long> tagIds = Arrays.asList(1L, 2L);
        noteTagService.batchAddTagsToNotes(noteIds, tagIds);

        // When - 移除标签
        noteTagService.batchRemoveTagsFromNotes(noteIds, tagIds);

        // Then - 所有记录应该被标记为删除
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getDeleted, (short)0);
        long activeCount = noteTagMapper.selectCount(query);
        assertEquals(0, activeCount);
    }

    @Test
    @DisplayName("事务回滚测试：批量添加标签")
    void testBatchAddTagsToNotes_TransactionRollback() {
        // Given
        List<Long> noteIds = Arrays.asList(1L);
        List<Long> tagIds = Arrays.asList(1L);

        // When & Then - 使用不存在的ID来触发异常
        assertThrows(BusinessException.class, () -> {
            // 使用不存在的笔记ID，这样会在Service方法内部抛出异常
            noteTagService.batchAddTagsToNotes(Arrays.asList(999L), tagIds);
        });

        // 验证数据被回滚
        LambdaQueryWrapper<NoteTag> query = new LambdaQueryWrapper<>();
        query.eq(NoteTag::getDeleted, (short)0);
        long count = noteTagMapper.selectCount(query);
        assertEquals(0, count);
    }
}

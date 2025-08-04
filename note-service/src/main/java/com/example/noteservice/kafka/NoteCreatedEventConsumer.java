package com.example.noteservice.kafka;

import com.example.noteservice.domain.event.NoteCreatedEvent;
import com.example.noteservice.service.AsyncProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class NoteCreatedEventConsumer {

    private final AsyncProcessingService asyncProcessingService;

    public NoteCreatedEventConsumer(AsyncProcessingService asyncProcessingService) {
        this.asyncProcessingService = asyncProcessingService;
    }

    @PostConstruct
    public void init() {
        log.info("笔记创建事件消费者已初始化");
    }

    @KafkaListener(
            topics = "note-created-events",
            groupId = "note-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNoteCreatedEvent(
            @Payload NoteCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("接收到笔记创建事件: noteId={}, userId={}, title={}, topic={}, partition={}, offset={}",
                    event.getNoteId(), event.getUserId(), event.getTitle(), topic, partition, offset);

            // 处理事件
            asyncProcessingService.processNoteCreatedEvent(event);

            // 手动确认消息已成功处理
            acknowledgment.acknowledge();

            log.info("笔记创建事件处理成功并已确认: noteId={}", event.getNoteId());

        } catch (Exception e) {
            log.error("处理笔记创建事件失败: noteId={}, 错误: {}", event.getNoteId(), e.getMessage(), e);

            // 不确认消息，让Kafka重试机制处理
            throw e; // 重新抛出异常触发重试
        }
    }
}
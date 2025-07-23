package com.example.noteservice.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.DistributionSummary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CustomMetricsConfig {

    @Resource
    private MeterRegistry meterRegistry;

    // 计数器
    private Counter noteQueryCounter;
    private Counter noteCreateCounter;
    private Counter noteUpdateCounter;
    private Counter noteDeleteCounter;
    private Counter noteCacheHitCounter;
    private Counter noteCacheMissCounter;
    private Counter noteErrorCounter;
    private Counter popularNotesQueryCounter;
    private Counter popularNotesPageQueryCounter;

    // 计时器
    private Timer noteQueryTimer;
    private Timer noteCreateTimer;
    private Timer noteUpdateTimer;
    private Timer noteDeleteTimer;
    private Timer cacheOperationTimer;
    private Timer popularNotesQueryTimer;
    private Timer popularNotesPageQueryTimer;

    // 分布摘要
    private DistributionSummary noteContentLengthSummary;
    private DistributionSummary noteTitleLengthSummary;

    // 仪表盘
    private AtomicLong activeNotesGauge;
    private AtomicLong totalNotesGauge;

    @PostConstruct
    public void init() {
        // 初始化计数器
        noteQueryCounter = Counter.builder("note.query.total")
                .description("笔记查询总次数")
                .register(meterRegistry);

        noteCreateCounter = Counter.builder("note.create.total")
                .description("笔记创建总次数")
                .register(meterRegistry);

        noteUpdateCounter = Counter.builder("note.update.total")
                .description("笔记更新总次数")
                .register(meterRegistry);

        noteDeleteCounter = Counter.builder("note.delete.total")
                .description("笔记删除总次数")
                .register(meterRegistry);

        noteCacheHitCounter = Counter.builder("note.cache.hit.total")
                .description("笔记缓存命中总次数")
                .register(meterRegistry);

        noteCacheMissCounter = Counter.builder("note.cache.miss.total")
                .description("笔记缓存未命中总次数")
                .register(meterRegistry);

        noteErrorCounter = Counter.builder("note.error.total")
                .description("笔记操作错误总次数")
                .register(meterRegistry);

        popularNotesQueryCounter = Counter.builder("note.popular.query.total")
                .description("热门笔记查询总次数")
                .register(meterRegistry);

        popularNotesPageQueryCounter = Counter.builder("note.popular.page.query.total")
                .description("热门笔记分页查询总次数")
                .register(meterRegistry);

        // 初始化计时器
        noteQueryTimer = Timer.builder("note.query.duration")
                .description("笔记查询耗时")
                .register(meterRegistry);

        noteCreateTimer = Timer.builder("note.create.duration")
                .description("笔记创建耗时")
                .register(meterRegistry);

        noteUpdateTimer = Timer.builder("note.update.duration")
                .description("笔记更新耗时")
                .register(meterRegistry);

        noteDeleteTimer = Timer.builder("note.delete.duration")
                .description("笔记删除耗时")
                .register(meterRegistry);

        cacheOperationTimer = Timer.builder("note.cache.operation.duration")
                .description("缓存操作耗时")
                .register(meterRegistry);

        popularNotesQueryTimer = Timer.builder("note.popular.query.duration")
                .description("热门笔记查询耗时")
                .register(meterRegistry);

        popularNotesPageQueryTimer = Timer.builder("note.popular.page.query.duration")
                .description("热门笔记分页查询耗时")
                .register(meterRegistry);

        // 初始化分布摘要
        noteContentLengthSummary = DistributionSummary.builder("note.content.length")
                .description("笔记内容长度分布")
                .register(meterRegistry);

        noteTitleLengthSummary = DistributionSummary.builder("note.title.length")
                .description("笔记标题长度分布")
                .register(meterRegistry);

        // 初始化仪表盘
        activeNotesGauge = new AtomicLong(0);
        totalNotesGauge = new AtomicLong(0);

        Gauge.builder("note.active.count", activeNotesGauge, AtomicLong::get)
                .description("活跃笔记数量")
                .register(meterRegistry);

        Gauge.builder("note.total.count", totalNotesGauge, AtomicLong::get)
                .description("总笔记数量")
                .register(meterRegistry);
    }

    // 计数器方法
    public void incrementNoteQuery() {
        noteQueryCounter.increment();
    }

    public void incrementNoteCreate() {
        noteCreateCounter.increment();
    }

    public void incrementNoteUpdate() {
        noteUpdateCounter.increment();
    }

    public void incrementNoteDelete() {
        noteDeleteCounter.increment();
    }

    public void incrementNoteCacheHit() {
        noteCacheHitCounter.increment();
    }

    public void incrementNoteCacheMiss() {
        noteCacheMissCounter.increment();
    }

    public void incrementNoteError() {
        noteErrorCounter.increment();
    }

    public void incrementPopularNotesQuery() {
        popularNotesQueryCounter.increment();
    }

    public void incrementPopularNotesPageQuery() {
        popularNotesPageQueryCounter.increment();
    }

    // 计时器方法
    public Timer.Sample startNoteQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopNoteQueryTimer(Timer.Sample sample) {
        sample.stop(noteQueryTimer);
    }

    public Timer.Sample startNoteCreateTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopNoteCreateTimer(Timer.Sample sample) {
        sample.stop(noteCreateTimer);
    }

    public Timer.Sample startNoteUpdateTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopNoteUpdateTimer(Timer.Sample sample) {
        sample.stop(noteUpdateTimer);
    }

    public Timer.Sample startNoteDeleteTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopNoteDeleteTimer(Timer.Sample sample) {
        sample.stop(noteDeleteTimer);
    }

    public Timer.Sample startCacheOperationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCacheOperationTimer(Timer.Sample sample) {
        sample.stop(cacheOperationTimer);
    }

    public Timer.Sample startPopularNotesQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPopularNotesQueryTimer(Timer.Sample sample) {
        sample.stop(popularNotesQueryTimer);
    }

    public Timer.Sample startPopularNotesPageQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPopularNotesPageQueryTimer(Timer.Sample sample) {
        sample.stop(popularNotesPageQueryTimer);
    }

    // 分布摘要方法
    public void recordNoteContentLength(int length) {
        noteContentLengthSummary.record(length);
    }

    public void recordNoteTitleLength(int length) {
        noteTitleLengthSummary.record(length);
    }

    // 仪表盘方法
    public void setActiveNotesCount(long count) {
        activeNotesGauge.set(count);
    }

    public void setTotalNotesCount(long count) {
        totalNotesGauge.set(count);
    }
}
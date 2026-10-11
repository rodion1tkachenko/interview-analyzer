package com.diploma.interview_analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // если @EnableAsync уже есть в другом классе, оставьте только одну аннотацию
public class AsyncConfig {

    // Пул по умолчанию для @Async (ffmpeg). Имя "taskExecutor" важно: см. примечание ниже
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(50);
        ex.setThreadNamePrefix("media-");
        ex.initialize();
        return ex;
    }

    // Только транскрибация, строго по одной задаче
    @Bean("sttExecutor")
    public Executor sttExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(1);
        ex.setMaxPoolSize(1);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("stt-");
        ex.initialize();
        return ex;
    }
}
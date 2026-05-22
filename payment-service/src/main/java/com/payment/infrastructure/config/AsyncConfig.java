package com.payment.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình Async cho non-blocking event publishing
 *
 * Sử dụng thread pool riêng cho async operations để tránh
 * block main thread khi publish Kafka events.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Task Executor cho async operations
     *
     * Thread pool config:
     * - Core pool size: 5 threads (luôn live)
     * - Max pool size: 20 threads (khi load cao)
     * - Queue capacity: 100 tasks (buffer)
     * - Thread name prefix: async-event-
     */
    @Bean(name = "eventTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core thread pool size - số lượng thread luôn live
        executor.setCorePoolSize(5);

        // Max thread pool size - số lượng thread tối đa
        executor.setMaxPoolSize(20);

        // Queue capacity - bufferSize cho tasks chờ xử lý
        executor.setQueueCapacity(100);

        // Thread name prefix - dễ debug
        executor.setThreadNamePrefix("async-event-");

        // Keep alive time - thời gian idle thread được giữ lại trước khi terminate
        executor.setKeepAliveSeconds(60);

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Await termination time - max time chờ tasks complete khi shutdown
        executor.setAwaitTerminationSeconds(30);

        // Rejected execution policy - khi queue full
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Async task rejected from queue. Task: {}", r.toString());
        });

        executor.initialize();

        log.info("Async event task executor initialized: coreSize={}, maxSize={}, queueCapacity={}",
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Exception Handler cho async methods
     *
     * Log lỗi khi async method throw exception thay vì fail silently.
     */
    @Bean
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async method execution failed: method={}, params={}, error={}",
                method.getName(),
                params,
                throwable.getMessage(),
                throwable
            );
        };
    }
}

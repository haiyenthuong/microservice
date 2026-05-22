package com.order.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration cho Order Service
 *
 * Cấu hình async task execution cho các operations như:
 * - Publishing events to Kafka
 * - Sending notifications
 * - Calling external APIs asynchronously
 *
 * Key Features:
 * - Custom thread pool với appropriate sizing
 * - Exception handling cho async tasks
 * - Graceful shutdown
 * - Thread naming để dễ debug
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * Cấu hình Task Executor cho async operations
     *
     * Thread Pool Sizing:
     * - Core Pool Size: 5 threads (luôn active)
     * - Max Pool Size: 20 threads (tăng khi load cao)
     * - Queue Capacity: 100 tasks (chờ đợi để process)
     * - Keep Alive: 60s (idle threads sẽ terminated sau 60s)
     *
     * @return Executor task executor
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool sizing
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);

        // Thread keep alive time
        executor.setKeepAliveSeconds(60);

        // Thread naming để dễ debug trong logs
        executor.setThreadNamePrefix("order-async-");

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Max wait time cho pending tasks trên shutdown
        executor.setAwaitTerminationSeconds(60);

        // Rejected execution policy (khi queue full)
        // CallerRunsPolicy = task sẽ chạy trong caller thread
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Initialize executor
        executor.initialize();

        log.info("Async Task Executor initialized - Core: {} | Max: {} | Queue: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Cấu hình Exception Handler cho async tasks
     *
     * Khi async task throw exception, handler này sẽ được gọi
     * để log error thay vì crash thread.
     *
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async method {} threw exception: {}",
                    method.getName(), throwable.getMessage(), throwable);

            log.error("Method parameters:");
            if (params != null) {
                for (Object param : params) {
                    log.error("  - {} ({})", param, param != null ? param.getClass().getName() : "null");
                }
            }

            // TODO: Implement additional error handling
            // - Send alert to operations team
            // - Publish to monitoring system
            // - Log to error tracking system (Sentry, etc.)
        };
    }
}

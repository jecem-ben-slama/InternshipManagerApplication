package com.iit.internship_manager.infrastucture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
@Configuration
@EnableAsync // This is the key annotation
public class AsyncConfig {
    @Bean(name = "emailExecutor")
public Executor emailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // Setting core and max to 1 ensures emails are sent one-by-one
    executor.setCorePoolSize(1); 
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("EmailWorker-");
    executor.initialize();
    return executor;
}
}
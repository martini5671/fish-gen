package com.martini.fish_gen.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// @Async methods run on Spring Boot's auto-configured applicationTaskExecutor (spring.task.execution.* properties)
@Configuration
@EnableAsync
public class AsyncConfiguration {
}

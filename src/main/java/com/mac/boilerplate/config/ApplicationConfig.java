package com.mac.boilerplate.config;

import com.mac.boilerplate.config.properties.TaskProperties;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaskProperties.class)
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean(destroyMethod = "close")
    ExecutorService taskVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

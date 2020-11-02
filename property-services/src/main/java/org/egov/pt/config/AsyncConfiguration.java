package org.egov.pt.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.*;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class AsyncConfiguration  {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        return executor;
    }
}
package com.bhbworkout.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsynConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //프로세스 개수
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("creating pool with core {}", processors);

        executor.setCorePoolSize(processors); // cpu에따라

        /*
        * 작업대기 수(50)도 꽉찼으면 *2 한만큼 늘림
        * 그럼에도 작업대기가 더 있다 -> 요청처리 못함 -> reject 함
        * */
        executor.setMaxPoolSize(processors * 2); // cpu에 따라

        /*
        * 10개 밖에 없는데 다 사용하고 있으면
        * 나머지 기다리는 작업대기(50)
        * */
        executor.setQueueCapacity(50); // 메모리에 따라 달라짐

        /*
        * 추가된 cpu 코어에 대해서 1분후에 수거한다.
        * */
        executor.setKeepAliveSeconds(60);

        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}

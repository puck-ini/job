package org.github.tasktest;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengchzh
 * @date 2021/12/13
 */

@Component
@Task(cron = "0/1 * * * * ? ")
@Slf4j
public class LogTask {

    public void log() {
        log.info("hello world : " + LocalDateTime.now());
    }
}

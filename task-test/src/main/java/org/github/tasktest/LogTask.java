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
@Task
@Slf4j
public class LogTask {

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    public void log() {
        log.info("" + COUNT.getAndIncrement());
//        log.info("hello world : " + LocalDateTime.now());
    }
}

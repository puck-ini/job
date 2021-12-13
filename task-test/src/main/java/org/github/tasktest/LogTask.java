package org.github.tasktest;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author zengchzh
 * @date 2021/12/13
 */

@Component
@Task
@Slf4j
public class LogTask {

    public void log() {
        log.info("hello world : " + LocalDateTime.now());
    }
}

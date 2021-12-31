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
@Slf4j
public class LogTask {

    @Task
    public void log() {
        log.info("hello world : " + LocalDateTime.now());
    }

    @Task(cron = "0/1 * * * * ? ")
    public void log1() {
        log.info("log1 : " + LocalDateTime.now());
    }

    @Task(cron = "0/2 * * * * ? ")
    public void log2() {
        log.info("log2 : " + LocalDateTime.now());
    }

    @Task(cron = "0/3 * * * * ? ")
    public void log3() {
        log.info("log3 : " + LocalDateTime.now());
    }

    @Task(cron = "0/4 * * * * ? ")
    public void log4() {
        log.info("log4 : " + LocalDateTime.now());
    }

    @Task(cron = "0/5 * * * * ? ")
    public void log5() {
        log.info("log5 : " + LocalDateTime.now());
    }

    @Task(cron = "0/59 * * * * ? ")
    public void log6() {
        log.info("log6 : " + LocalDateTime.now());
    }

    @Task(cron = "0/30 * * * * ? ")
    public void log7() {
        log.info("log7 : " + LocalDateTime.now());
    }

    @Task(cron = "0/29 * * * * ? ")
    public void log8() {
        log.info("log8 : " + LocalDateTime.now());
    }
}

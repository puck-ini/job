package org.github.admin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.TimerTask;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@SpringBootTest
class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Test
    void addThread() {
        schedulerService.addCheckThread();
    }

    @DisplayName("模拟单机下调度")
//    @Test
    @RepeatedTest(value = 3)
    void testThread() {
        addThread();
//        startTrigger();
        taskTriggerService.startTrigger(1L);
        sleep(30);
        stopTrigger();
        stopThread();
        sleep(6);
    }


    @DisplayName("模拟集群下调度")
    @Test
    void test10Thread() {
        for (int i = 0; i < 10; i++) {
            addThread();
        }
        startTrigger();
        sleep(60);
        stopTrigger();
        stopThread();
        sleep(6);
    }

    @DisplayName("测试任务关闭")
//    @RepeatedTest(value = 3)
    @Test
    void testCancel() throws InterruptedException {
        schedulerService.addCheckThread();
        TimerTask timerTask = new LocalTask(() -> {
            log.info(LocalDateTime.now().toString());
        }, "0/1 * * * * ? ");
        schedulerService.addTask(timerTask);
        TimeUnit.SECONDS.sleep(10);
        timerTask.cancel();
    }

    @Test
    void startTrigger() {
        taskTriggerService.startTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }

    @Test
    void stopTrigger() {
        taskTriggerService.stopTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }


    @Test
    void stopThread() {
        schedulerService.stop();
    }


    void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testThreadRunning() {
        addThread();
        startTrigger();
        sleep(10);
        log.info("stop thread -------------------------" + LocalDateTime.now());
        stopThread();
        sleep(10);
    }

    @Test
    void testLocalTaskException() {
        addThread();
        schedulerService.addTask(new LocalTask(() -> {
            if (true) {
                throw new NullPointerException("test exception");
            }
        }, "0/1 * * * * ? "));
        sleep(10);
        stopThread();
        sleep(10);
    }

    @AfterEach
    void doAfter() {
        log.info("doAfter - " + LocalDateTime.now());
        log.info("stop trigger - " + LocalDateTime.now());
        stopTrigger();
        log.info("stop scheduler thread - " + LocalDateTime.now());
        stopThread();
    }

}
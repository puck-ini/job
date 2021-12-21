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

    @DisplayName("模拟单机下调度")
    @Test
//    @RepeatedTest(value = 10)
    void testThread() {
        schedulerService.addCheckThread();
        taskTriggerService.startTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
//        taskTriggerService.startTrigger(1L);
    }


    @DisplayName("模拟集群下调度")
    @Test
    void test10Thread() {
        for (int i = 0; i < 10; i++) {
            schedulerService.addCheckThread();
        }
        taskTriggerService.startTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
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
    void stopTrigger() {
        taskTriggerService.stopTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }


    @AfterEach
    void sleep() {
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("sleep end - " + LocalDateTime.now());
        log.info("stop trigger - " + LocalDateTime.now());
        stopTrigger();
        log.info("stop scheduler thread - " + LocalDateTime.now());
        schedulerService.stop();
    }

}
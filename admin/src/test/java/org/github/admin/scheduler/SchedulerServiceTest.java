package org.github.admin.scheduler;

import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Test
    void initThread() {
        schedulerService.addCheckThread();
    }


    @Test
    void test10Thread() {
        for (int i = 0; i < 10; i++) {
            schedulerService.addCheckThread();
        }
        taskTriggerService.startTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }


    @AfterEach
    void sleep() {
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        taskTriggerService.stopTrigger(taskTriggerService.list().getContent().stream().map(TaskTrigger::getId).collect(Collectors.toList()));
        schedulerService.stop();
    }

}
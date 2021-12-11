package org.github.admin;

import org.github.admin.entity.TaskTrigger;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
class TaskSchedulerTest {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private TaskTriggerService taskTriggerService;


    @BeforeEach
    @Test
    void init() {
        taskScheduler.start();
    }


    @Test
    void startTrigger() {
        List<TaskTrigger> taskTriggerList = taskTriggerService.list().getContent();
        taskTriggerService.startTrigger(taskTriggerList.stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }

    @AfterEach
    void sleep() throws InterruptedException {
        TimeUnit.SECONDS.sleep(30);
        List<TaskTrigger> taskTriggerList = taskTriggerService.list().getContent();
        taskTriggerService.stopTrigger(taskTriggerList.stream().map(TaskTrigger::getId).collect(Collectors.toList()));
        taskScheduler.stop();
    }

}
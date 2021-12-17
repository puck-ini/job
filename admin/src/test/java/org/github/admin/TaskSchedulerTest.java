package org.github.admin;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.scheduler.SchedulerService;
import org.github.admin.scheduler.TaskScheduler;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class TaskSchedulerTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Test
    void init() {
        schedulerService.addCheckThread();
        List<TaskTrigger> taskTriggerList = taskTriggerService.list().getContent();
        taskTriggerService.startTrigger(taskTriggerList.stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }


    @Test
    void startTrigger() {
        List<TaskTrigger> taskTriggerList = taskTriggerService.list().getContent();
        taskTriggerService.startTrigger(taskTriggerList.stream().map(TaskTrigger::getId).collect(Collectors.toList()));
    }

    @Test
    void addLocalTask() throws InterruptedException {
        LocalTask task = new LocalTask(() -> {
            log.info("hello world : " + LocalDateTime.now());
        }, "0/1 * * * * ? ");
        schedulerService.addTask(task);
    }

    @AfterEach
    void sleep() throws InterruptedException {
        TimeUnit.SECONDS.sleep(20);
        List<TaskTrigger> taskTriggerList = taskTriggerService.list().getContent();
        taskTriggerService.stopTrigger(taskTriggerList.stream().map(TaskTrigger::getId).collect(Collectors.toList()));
        schedulerService.stop();
    }

}
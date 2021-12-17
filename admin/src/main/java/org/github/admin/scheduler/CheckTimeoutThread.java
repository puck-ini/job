package org.github.admin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.service.TaskTriggerService;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengchzh
 * @date 2021/12/16
 */

@Slf4j
public class CheckTimeoutThread extends Thread {

    private static final long DELAY_TIME = 5000L;

    private static final long PRE_READ_TIME = 5000L;

    private static final int PRE_READ_SIZE = 1000;

    private final TaskTriggerService taskTriggerService;

    private final TaskScheduler taskScheduler;

    private volatile AtomicBoolean threadState = new AtomicBoolean(START);

    private static final boolean START = true;

    private static final boolean STOP = false;

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public CheckTimeoutThread(TaskTriggerService taskTriggerService, TaskScheduler taskScheduler) {
        this.taskTriggerService = taskTriggerService;
        this.taskScheduler = taskScheduler;
        setDaemon(true);
        setName("CheckTimeout-" + COUNTER.getAndIncrement());
    }

    @Override
    public void run() {
        while (threadState.get() == START) {
            try {
                checkTimeout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void checkTimeout() {
        long start = System.currentTimeMillis();
        boolean checkSuccess = false;
        List<TaskTrigger> taskTriggerList = taskTriggerService.getDeadlineTrigger(PRE_READ_TIME, PRE_READ_SIZE);
        if (!CollectionUtils.isEmpty(taskTriggerList)) {
            for (TaskTrigger trigger : taskTriggerList) {
                RemoteTask task = new RemoteTask(trigger);
                taskScheduler.addTask(task);
                for (Point point : task.getPointSet()) {
                    if (!taskScheduler.contains(point)) {
                        CompletableFuture.runAsync(() -> {
                            taskScheduler.registerInvocation(point, new TaskInvocation(point, this.taskScheduler));
                        });
                    }
                }
            }
            taskTriggerService.refreshTriggerTime(taskTriggerList);
            checkSuccess = true;
        }
        long cost = System.currentTimeMillis() - start;
        if (cost < TaskScheduler.TICK) {
            delayCheck(checkSuccess);
        }
    }


    private void delayCheck(boolean checkSuccess) {
        try {
            TimeUnit.MILLISECONDS.sleep(checkSuccess ? TaskScheduler.TICK : DELAY_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TaskScheduler getScheduler() {
        return taskScheduler;
    }

    public void toStop() {
        threadState.compareAndSet(START, STOP);
        taskScheduler.stop();
    }
}

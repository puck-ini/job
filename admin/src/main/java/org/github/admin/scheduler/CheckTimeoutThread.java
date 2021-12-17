package org.github.admin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.service.TaskTriggerService;

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
                long start = System.currentTimeMillis();
                boolean checkSuccess = taskTriggerService.checkTimeout(taskScheduler);
                long cost = System.currentTimeMillis() - start;
                if (cost < TaskScheduler.TICK) {
                    delayCheck(checkSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

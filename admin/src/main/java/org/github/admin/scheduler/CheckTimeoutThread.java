package org.github.admin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.service.TaskTriggerService;

import java.util.concurrent.TimeUnit;
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

    private static final boolean START = true;

    private static final boolean STOP = false;

    private volatile boolean state = START;

    public static final long PRE_READ_TIME = 5000L;

    private static final int PRE_READ_SIZE = 1000;

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public CheckTimeoutThread(TaskTriggerService taskTriggerService, TaskScheduler taskScheduler) {
        this.taskTriggerService = taskTriggerService;
        this.taskScheduler = taskScheduler;
        setDaemon(true);
        setName("CheckTimeout-" + COUNTER.getAndIncrement());
    }

    @Override
    public void run() {
        while (state == START) {
            try {
                long start = System.currentTimeMillis();
                boolean addSuccess = taskTriggerService.addTimeoutTask(
                        taskScheduler,
                        System.currentTimeMillis() + PRE_READ_TIME,
                        PRE_READ_SIZE
                );
                long cost = System.currentTimeMillis() - start;
//                log.info("check cost : " + cost + ", addSuccess " + addSuccess);
                if (cost < TaskScheduler.TICK) {
                    delayCheck(addSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void delayCheck(boolean addSuccess) {
        try {
            TimeUnit.MILLISECONDS.sleep((addSuccess ? TaskScheduler.TICK : DELAY_TIME) - System.currentTimeMillis() % TaskScheduler.TICK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TaskScheduler getScheduler() {
        return taskScheduler;
    }

    public void toStop() {
        state = STOP;
        taskScheduler.stop();
    }
}

package org.github.admin;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.entity.Point;
import org.github.admin.entity.TaskTrigger;
import org.github.admin.service.TaskTriggerService;
import org.github.common.TaskDesc;
import org.github.common.TaskReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
//@Component
public class TaskScheduler {

    private static final long TICK  = 1000L;

    private static final long DELAY_TIME = 5000L;

    private static final long PRE_READ_TIME = DELAY_TIME;

    private static final Integer PRE_READ_SIZE = 1000;

    private volatile boolean checkThreadStop = false;

    private volatile boolean triggerThreadStop = false;

    @Autowired
    private TaskTriggerService taskTriggerService;

    private static volatile Map<Integer, List<TaskTrigger>> triggerMap = new ConcurrentHashMap<>();

    private Map<Point, TaskInvocation> invocationMap = new ConcurrentHashMap<>();

    private final ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("scheduler-" + r.hashCode());
            return thread;
        }
    };

    private final Thread checkThread = threadFactory.newThread(() -> {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!checkThreadStop) {
            log.info(Thread.currentThread().getName());
            checkTimeout();
        }
    });

    private final Thread triggerThread = threadFactory.newThread(() -> {
        while (!triggerThreadStop) {
            log.info(Thread.currentThread().getName());
            trigger();
        }
    });

    public void start() {
        checkThread.start();
        triggerThread.start();
    }

    private void checkTimeout() {
        try {
            long start = System.currentTimeMillis();
            boolean checkSuccess = false;
            List<TaskTrigger> taskTriggerList = taskTriggerService.getDeadlineTrigger(PRE_READ_TIME, PRE_READ_SIZE);
            if (!CollectionUtils.isEmpty(taskTriggerList)) {
                addTrigger(taskTriggerList);
                taskTriggerService.refreshTriggerTime(taskTriggerList);
                checkSuccess = true;
            }
            long cost = System.currentTimeMillis() - start;
            if (cost < TICK) {
                delayCheck(checkSuccess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void delayCheck(boolean checkSuccess) {
        try {
            TimeUnit.MILLISECONDS.sleep(checkSuccess ? TICK : DELAY_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addTrigger(List<TaskTrigger> taskTriggerList) {
        for (TaskTrigger trigger : taskTriggerList) {
            int index = (int) ((trigger.getNextTime() / 1000) % 60);
            List<TaskTrigger> triggerList = triggerMap.computeIfAbsent(index, k -> new ArrayList<>());
            triggerList.add(trigger);
        }
    }

    private void trigger() {
        try {
            int nowSecond = waitForNextTick();
            List<TaskTrigger> triggerList = triggerMap.remove(nowSecond);
            if (!CollectionUtils.isEmpty(triggerList)) {
                triggerList.forEach(this::invoke);
                triggerList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int waitForNextTick() {
        try {
            Thread.sleep(TICK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    private void invoke(TaskTrigger taskTrigger) {
        log.info("trigger : " + taskTrigger.toString());
        List<Point> pointList = taskTrigger.getTaskInfo().getTaskGroup().getPointList();
        TaskInvocation invocation = invocationMap.computeIfAbsent(
                pointList.get(new Random().nextInt(pointList.size())),
                k -> new TaskInvocation(k, invocationMap)
        );
        TaskDesc taskDesc = taskTrigger.getTaskInfo().getTaskDesc();
        TaskReq req = TaskReq.builder()
                .requestId(UUID.randomUUID().toString())
                .className(taskDesc.getClassName())
                .methodName(taskDesc.getMethodName())
                .parameterTypes(parseTypesJson(taskDesc.getParameterTypes()))
                .parameters(parseParaJson(taskTrigger.getParameters()))
                .build();
        invocation.invoke(req);
    }

    private Class[] parseTypesJson(String json) {
        if (Objects.isNull(json)) {
            return new Class[]{};
        }
        List<Class> objects = JSON.parseArray(json, Class.class);
        Class[] arr = new Class[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            arr[i] = objects.get(i);
        }
        return arr;
    }

    private Object[] parseParaJson(String json) {
        if (Objects.isNull(json)) {
            return new Object[]{};
        }
        List<Object> objects = JSON.parseArray(json, Object.class);
        return objects.toArray();
    }

    public void stop() {
        checkThreadStop = true;
        triggerThreadStop = true;
    }


}

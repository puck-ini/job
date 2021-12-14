package org.github.admin.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.*;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.model.task.TimerTask;
import org.github.common.ServiceObject;
import org.github.common.TaskReq;
import org.github.common.ZkRegister;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TaskScheduler {

    private static final long TICK  = 1000L;

    private static final long DELAY_TIME = 5000L;

    private static final long PRE_READ_TIME = DELAY_TIME;

    private static final Integer PRE_READ_SIZE = 1000;

    private volatile boolean checkThreadStop = false;

    private volatile boolean triggerThreadStop = false;

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Autowired
    private ZkRegister zkRegister;

    private static volatile Map<Integer, List<TimerTask>> taskMap = new ConcurrentHashMap<>();

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
            checkTimeout();
        }
    });

    private final Thread triggerThread = threadFactory.newThread(() -> {
        while (!triggerThreadStop) {
            trigger();
        }
    });

    public void start() {
        checkThread.start();
        triggerThread.start();
        startPreReq();
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
            List<TimerTask> triggerList = taskMap.computeIfAbsent(index, k -> new ArrayList<>());
            triggerList.add(new RemoteTask(trigger));
        }
    }

    private void trigger() {
        try {
            int nowSecond = waitForNextTick();
            List<TimerTask> taskList = taskMap.remove(nowSecond);
            if (!CollectionUtils.isEmpty(taskList)) {
                taskList.forEach(this::invoke);
                taskList.clear();
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

    private void invoke(TimerTask task) {
        if (task instanceof RemoteTask) {
            RemoteTask remoteTask = (RemoteTask) task;
            Set<Point> pointSet = remoteTask.getPointSet();
            TaskInvocation invocation = invocationMap.computeIfAbsent(
                    (Point) pointSet.toArray()[new Random().nextInt(pointSet.size())],
                    k -> new TaskInvocation(k, invocationMap)
            );
            TaskReq req = TaskReq.builder()
                    .requestId(UUID.randomUUID().toString())
                    .className(remoteTask.getClassName())
                    .methodName(remoteTask.getMethodName())
                    .parameterTypes(parseTypesJson(remoteTask.getParameterTypes()))
                    .parameters(parseParaJson(remoteTask.getParameters()))
                    .build();
            invocation.invoke(req);
        } else if (task instanceof LocalTask) {
            LocalTask localTask = (LocalTask) task;
            localTask.run();
            addTask(localTask);
        }

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

    private void startPreReq() {
        LocalTask task = new LocalTask(() -> {
            List<ServiceObject> soList = zkRegister.getAll();
            soList.forEach(so -> {
                TaskInvocation invocation = invocationMap.computeIfAbsent(
                        new Point(so.getIp(), so.getPort()),
                        k -> new TaskInvocation(k, invocationMap)
                );
                invocation.preRead();
            });
        }, "0/30 * * * * ? ");
        addTask(task);
    }

    public static void addTask(LocalTask task) {
        if (Objects.isNull(task)) {
            return;
        }
        int index = (int) (((task.getNextTime()) / 1000) % 60);
        List<TimerTask> taskList = taskMap.computeIfAbsent(index, k -> new ArrayList<>());
        taskList.add(task);
    }

    public void stop() {
        checkThreadStop = true;
        triggerThreadStop = true;
    }


}

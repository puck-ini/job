package org.github.admin.scheduler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.*;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.model.task.TimerTask;
import org.github.common.TaskReq;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
public class TaskScheduler {

    public static final long TICK  = 1000L;

    private volatile AtomicBoolean schedulerState = new AtomicBoolean(STOP);

    private static final boolean START = true;

    private static final boolean STOP = false;

    private volatile Map<Integer, List<TimerTask>> taskMap = new ConcurrentHashMap<>();

    private volatile Map<Point, Invocation> invocationMap = new ConcurrentHashMap<>();

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("scheduler-" + COUNTER.getAndIncrement());
            return thread;
        }
    };

    private final Thread triggerThread = threadFactory.newThread(() -> {
        while (schedulerState.get() == START) {
            run();
        }
    });

    public void start() {
        if (schedulerState.getAndSet(START) == STOP) {
            triggerThread.start();
        }
    }

    public void addTask(TimerTask task) {
        start();
        if (Objects.isNull(task)) {
            return;
        }
        int index = (int) ((task.getNextTime() / 1000) % 60);
        List<TimerTask> taskList = taskMap.computeIfAbsent(index, k -> new ArrayList<>());
        taskList.add(task);
    }

    private void run() {
        try {
            int nowSecond = waitForNextTick();
            List<TimerTask> taskList = taskMap.remove(nowSecond);
            if (!CollectionUtils.isEmpty(taskList)) {
                log.info(Thread.currentThread().getName() + " - invoke task size - " + taskList.size());
                taskList.forEach(this::runTask);
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

    public void runTask(TimerTask task) {
        if (task instanceof RemoteTask) {
            RemoteTask remoteTask = (RemoteTask) task;
            Set<Point> pointSet = remoteTask.getPointSet();
            boolean invokeSuccess = false;
            for (Point point : pointSet) {
                Invocation invocation = invocationMap.computeIfAbsent(
                        point,
                        k -> new TaskInvocation(k, this)
                );
                if (invocation.isAvailable()) {
                    TaskReq req = TaskReq.builder()
                            .requestId(UUID.randomUUID().toString())
                            .className(remoteTask.getClassName())
                            .methodName(remoteTask.getMethodName())
                            .parameterTypes(parseTypesJson(remoteTask.getParameterTypes()))
                            .parameters(parseParaJson(remoteTask.getParameters()))
                            .build();
                    invocation.invoke(req);
                    invokeSuccess = true;
                    break;
                } else {
                    CompletableFuture.runAsync(invocation::connnect);
                }
            }
            if (!invokeSuccess) {
                log.error(pointSet + " unavailable");
            }
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


    public Invocation registerInvocation(Point point, Invocation invocation) {
        Invocation invocation1 = invocationMap.computeIfAbsent(point, k -> invocation);
        invocation1.connnect();
        return invocation1;
    }

    public boolean contains(Point point) {
        return invocationMap.containsKey(point);
    }

    public Invocation remove(Point point) {
        return invocationMap.remove(point);
    }

    public void stop() {
        schedulerState.compareAndSet(START, STOP);
        Iterator<Map.Entry<Point, Invocation>> iterator = invocationMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Point, Invocation> entry = iterator.next();
            entry.getValue().disconnect();
            iterator.remove();
        }
    }


}

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

    private final AtomicBoolean schedulerState = new AtomicBoolean(STOP);

    private static final boolean START = true;

    private static final boolean STOP = false;

    private final Map<Integer, List<TimerTask>> taskMap = new ConcurrentHashMap<>();

    private final Map<Point, Invocation> invocationMap = new ConcurrentHashMap<>();

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
        log.info("run task - " + task.getName());
        if (task instanceof RemoteTask) {
            RemoteTask remoteTask = (RemoteTask) task;
            Set<Point> pointSet = remoteTask.getPointSet();
            Invocation invocation = getInvocation(pointSet);
            if (Objects.isNull(invocation)) {
                log.error(pointSet + " unavailable");
                return;
            }
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
            if (!localTask.isCancel()) {
                addTask(localTask);
            }
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

    private Invocation getInvocation(Set<Point> pointSet) {
        Invocation invocation;
        for (Point point : pointSet) {
            invocation = invocationMap.computeIfAbsent(
                    point,
                    k -> new InvocationWrapper(new TaskInvocation(k))
            );
            if (invocation.isAvailable()) {
                return invocation;
            } else {
                CompletableFuture.runAsync(invocation::connnect);
            }
        }
        return null;
    }

    public Invocation registerInvocation(Point point, Invocation invocation) {
        Invocation invocationWrapper = invocationMap.computeIfAbsent(point, k -> new InvocationWrapper(invocation));
        CompletableFuture.runAsync(invocationWrapper::connnect);
        return invocation;
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

    class InvocationWrapper implements Invocation {

        private final Invocation invocation;

        private final AtomicInteger count = new AtomicInteger(0);

        private static final int MAX_COUNT = 5;

        private LocalTask reConnectTask;

        public InvocationWrapper(Invocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public synchronized void connnect() {
            if (Objects.nonNull(reConnectTask)) {
                return;
            }
            if (count.get() < MAX_COUNT) {
                invocation.connnect();
                if (Objects.isNull(reConnectTask)) {
                    reConnectTask = new LocalTask("reConnectTask-" + getPoint().getIp(), () -> {
                        if (invocation.isAvailable() || count.getAndIncrement() >= MAX_COUNT) {
                            reConnectTask.cancel();
                        } else {
                            CompletableFuture.runAsync(invocation::connnect);
                        }
                    }, "0/6 * * * * ? ");
                    log.info(Thread.currentThread().getName() + " add reconnect task");
                    addTask(reConnectTask);
                }
            }
            invocation.connnect();
        }

        @Override
        public void invoke(TaskReq req) {
            invocation.invoke(req);
        }

        @Override
        public void disconnect() {
            invocation.disconnect();
            remove(getPoint());
        }

        @Override
        public boolean isAvailable() {
            return invocation.isAvailable();
        }

        @Override
        public Point getPoint() {
            return invocation.getPoint();
        }
    }
}

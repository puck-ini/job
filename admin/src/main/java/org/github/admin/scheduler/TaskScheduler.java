package org.github.admin.scheduler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.*;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.model.task.TimerTask;
import org.github.common.TaskReq;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
public class TaskScheduler {

    public static final long TICK  = 1000L;

    private final AtomicInteger schedulerState = new AtomicInteger(INIT);

    private static final int INIT = 0;

    private static final int START = 1;

    private static final int STOP = 2;

    private final Map<Integer, List<TimerTask>> taskMap = new ConcurrentHashMap<>();

    private final Map<Point, Invocation> invocationMap = new ConcurrentHashMap<>();

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private long cost = 0;

    private final ThreadFactory threadFactory = r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("scheduler-" + COUNTER.getAndIncrement());
        return thread;
    };

    private final Thread triggerThread = threadFactory.newThread(() -> {
        while (schedulerState.get() == START) {
            try {
                run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        runPendingTask();
        clearInvocation();
    });

    public void start() {
        switch (schedulerState.get()) {
            case INIT: {
                if (schedulerState.compareAndSet(INIT, START)) {
                    triggerThread.start();
                }
                break;
            }
            case STOP:
                log.warn("cannot start");
            case START:
            default:
                break;
        }
    }

    public void addTask(TimerTask task) {
        start();
        if (Objects.isNull(task) || !isAvailable()) {
            return;
        }
        int index = (int) ((task.getNextTime() / 1000) % 60);
        List<TimerTask> taskList = taskMap.computeIfAbsent(index, k -> new ArrayList<>());
        taskList.add(task);
    }

    private void run() {
        int nowSecond = waitForNextTick(cost);
        List<TimerTask> taskList = taskMap.remove(nowSecond);
        if (!CollectionUtils.isEmpty(taskList)) {
            long start = System.currentTimeMillis();
            taskList.forEach(this::runTask);
            taskList.clear();
            cost = System.currentTimeMillis() - start;
        }
    }

    private int waitForNextTick(long cost) {
        if (cost < TICK) {
            try {
                Thread.sleep(TICK - cost);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    private void runTask(TimerTask task) {
        try {
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
        } catch (Exception e) {
            log.error(task.getName() + " run fail ", e);
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
        List<Invocation> invocationList = new ArrayList<>();
        for (Point point : pointSet) {
            invocation = invocationMap.computeIfAbsent(
                    point,
                    k -> new InvocationWrapper(new TaskInvocation(k))
            );
            if (invocation.isAvailable()) {
                invocationList.add(invocation);
            } else {
                CompletableFuture.runAsync(invocation::connnect);
            }
        }
        if (!invocationList.isEmpty()) {
            return invocationList.get(new Random().nextInt(invocationList.size()));
        }
        return null;
    }

    public Invocation registerInvocation(Point point, Invocation invocation) {
        if (contains(point)) {
            return invocationMap.get(point);
        }
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
        schedulerState.set(STOP);
    }

    private void runPendingTask() {
        int delayTime = (int) (CheckTimeoutThread.PRE_READ_TIME / 1000);
        for (int i = 0; i < delayTime; i++) {
            run();
        }
    }

    private void clearInvocation() {
        Iterator<Map.Entry<Point, Invocation>> iterator = invocationMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Point, Invocation> entry = iterator.next();
            entry.getValue().disconnect();
            iterator.remove();
        }
    }

    public boolean isAvailable() {
        return schedulerState.get() != STOP;
    }

    class InvocationWrapper implements Invocation {

        private final Invocation invocation;

        private final AtomicInteger reconnectCount = new AtomicInteger(0);

        private final AtomicInteger invokeFail = new AtomicInteger(0);

        private static final int MAX_COUNT = 5;

        private LocalTask reconnectTask;

        public InvocationWrapper(Invocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public synchronized void connnect() {
            if (Objects.nonNull(reconnectTask)) {
                return;
            }
            if (reconnectCount.get() < MAX_COUNT) {
                invocation.connnect();
                if (Objects.isNull(reconnectTask)) {
                    initReconnectTask();
                    addTask(reconnectTask);
                }
            }
            invocation.connnect();
        }

        private void initReconnectTask() {
            reconnectTask = new LocalTask("reconnectTask-" + getPoint().getIp(), () -> {
                if (invocation.isAvailable() || reconnectCount.getAndIncrement() >= MAX_COUNT) {
                    reconnectTask.cancel();
                } else {
                    CompletableFuture.runAsync(invocation::connnect);
                }
            }, "0/6 * * * * ? ");
        }

        @Override
        public void invoke(TaskReq req) {
            if (invokeFail.get() > MAX_COUNT) {
                return;
            }
            try {
                invocation.invoke(req);
            } catch (Exception e) {
                e.printStackTrace();
                if (invokeFail.getAndIncrement() == 0) {
                    addTask(reconnectTask);
                }
            }
        }

        @Override
        public void disconnect() {
            invocation.disconnect();
            remove(getPoint());
        }

        @Override
        public boolean isAvailable() {
            return invocation.isAvailable() && invokeFail.get() < MAX_COUNT;
        }

        @Override
        public Point getPoint() {
            return invocation.getPoint();
        }
    }
}

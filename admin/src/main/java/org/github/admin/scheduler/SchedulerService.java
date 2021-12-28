package org.github.admin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskGroup;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.TimerTask;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.service.TaskTriggerService;
import org.github.common.ServiceObject;
import org.github.common.ZkRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.IntStream;


/**
 * @author zengchzh
 * @date 2021/12/16
 */

@Slf4j
@Component
public class SchedulerService implements SmartLifecycle {

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Autowired(required = false)
    private ZkRegister zkRegister;

    @Value("${scheduler.thread.max-size:1}")
    private int size;

    @Value("${scheduler.thread.auto-start:true}")
    private boolean auto;

    @Value("${zk.enable:false}")
    private boolean zkEnable;


    private Map<String, CheckTimeoutThread> threadMap = new HashMap<>();

    public void addCheckThread() {
        if (threadMap.values().size() < size) {
            TaskScheduler scheduler = new TaskScheduler();
            if (zkEnable) {
                preGetTaskInfo(scheduler);
            }
            CheckTimeoutThread timeoutThread = new CheckTimeoutThread(taskTriggerService, scheduler);
            timeoutThread.start();
            threadMap.put(timeoutThread.getName(), timeoutThread);
        }
    }

    private void preGetTaskInfo(TaskScheduler scheduler) {
        LocalTask task = new LocalTask("GetTaskInfo", () -> {
            List<ServiceObject> soList = zkRegister.getAll();
            soList.forEach(so -> {
                TaskGroup taskGroup = taskGroupRepo.findByName(so.getGroupName());
                Point point = new Point(so.getIp(), so.getPort());
                if (Objects.isNull(taskGroup)) {
                    taskGroup = new TaskGroup();
                    taskGroup.setName(so.getGroupName());
                }
                taskGroup.getPointSet().add(point);
                taskGroupRepo.save(taskGroup);
                scheduler.registerInvocation(point, new TaskInvocation(point));
            });
        }, "0/3 * * * * ? ");
        scheduler.addTask(task);
    }

    public void addTask(TimerTask task) {
        CheckTimeoutThread timeoutThread = randomGet();
        timeoutThread.getScheduler().addTask(task);
    }


    private CheckTimeoutThread randomGet() {
        return (CheckTimeoutThread) threadMap.values().toArray()[new Random().nextInt(threadMap.values().size())];
    }

    public void register(Point point, Invocation invocation) {
        for (CheckTimeoutThread thread : threadMap.values()) {
            thread.getScheduler().registerInvocation(point, invocation);
        }
    }


    @Override
    public void start() {
        IntStream.range(0, size).forEach(i -> addCheckThread());
        auto = false;
    }

    @Override
    public void stop() {
        Iterator<Map.Entry<String, CheckTimeoutThread>> iterator = threadMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CheckTimeoutThread> entry = iterator.next();
            CheckTimeoutThread timeoutThread = entry.getValue();
            timeoutThread.toStop();
            iterator.remove();
        }
        try {
            Thread.sleep(CheckTimeoutThread.PRE_READ_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return !auto;
    }


}

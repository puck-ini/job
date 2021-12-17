package org.github.admin.scheduler;

import org.github.admin.model.entity.Point;
import org.github.admin.model.task.LocalTask;
import org.github.admin.model.task.TimerTask;
import org.github.admin.service.TaskTriggerService;
import org.github.common.ServiceObject;
import org.github.common.ZkRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * @author zengchzh
 * @date 2021/12/16
 */

@Component
public class SchedulerService {

    @Autowired
    private TaskTriggerService taskTriggerService;

    @Autowired
    private ZkRegister zkRegister;

    private int size = 1;


    private Map<String, CheckTimeoutThread> threadMap = new HashMap<>();


    public void addCheckThread() {
        if (threadMap.values().size() < size) {
            TaskScheduler scheduler = new TaskScheduler();
            scheduler.start();
            preConnect(scheduler);
            CheckTimeoutThread timeoutThread = new CheckTimeoutThread(taskTriggerService, scheduler);
            timeoutThread.start();
            threadMap.put(timeoutThread.getName(), timeoutThread);
        }
    }

    private void preConnect(TaskScheduler scheduler) {
        LocalTask task = new LocalTask(() -> {
            List<ServiceObject> soList = zkRegister.getAll();
            soList.forEach(so -> {
                Point point = new Point(so.getIp(), so.getPort());
                TaskInvocation invocation
                        = (TaskInvocation) scheduler.registerInvocation(point, new TaskInvocation(point, scheduler));
                invocation.preRead();
            });
        }, "0/30 * * * * ? ");
        scheduler.addTask(task);
    }

    public void addTask(TimerTask task) {
        CheckTimeoutThread timeoutThread = randomGet();
        timeoutThread.getScheduler().addTask(task);
    }


    private CheckTimeoutThread randomGet() {
        return (CheckTimeoutThread) threadMap.values().toArray()[new Random().nextInt(threadMap.values().size())];
    }


    public void stop() {
        for (CheckTimeoutThread timeoutThread : threadMap.values()) {
            timeoutThread.toStop();
        }
    }



}

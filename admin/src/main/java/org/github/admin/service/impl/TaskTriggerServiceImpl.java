package org.github.admin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.repo.TaskTriggerRepo;
import org.github.admin.model.req.CreateTriggerReq;
import org.github.admin.scheduler.CheckTimeoutThread;
import org.github.admin.scheduler.TaskInvocation;
import org.github.admin.scheduler.TaskScheduler;
import org.github.admin.service.TaskTriggerService;
import org.github.admin.util.CronExpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
@Service
public class TaskTriggerServiceImpl implements TaskTriggerService {

    @Autowired
    private TaskTriggerRepo taskTriggerRepo;

    @Autowired
    private TaskInfoRepo taskInfoRepo;

    @Autowired
    private TaskLockService lockService;

    private static final String LOCK_NAME = "task_lock";

    private static final long DELAY_START_TIME = CheckTimeoutThread.PRE_READ_TIME;

    @Override
    public Page<TaskTrigger> list() {
        return taskTriggerRepo.findAll(PageRequest.of(0, 10));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(CreateTriggerReq req) {
        taskInfoRepo.findById(req.getTaskId()).ifPresent(taskInfo -> {
            TaskTrigger taskTrigger = new TaskTrigger();
            taskTrigger.setParameters(req.getParameters());
            taskTrigger.setCronExpression(req.getCronExpression());
            taskTrigger.setTaskInfo(taskInfo);
            taskInfo.getTriggerSet().add(taskTrigger);
            taskInfoRepo.save(taskInfo);
        });
    }

    @Override
    public void startTrigger(Long triggerId) {
        taskTriggerRepo.findById(triggerId).ifPresent(taskTrigger -> {
            taskTrigger.setStatus(TaskTrigger.TriggerStatus.RUNNING);
            taskTrigger.setStartTime(System.currentTimeMillis());
            taskTrigger.setLastTime(taskTrigger.getNextTime());
            taskTrigger.setNextTime(CronExpUtil.getNextTime(
                    taskTrigger.getCronExpression(),
                    new Date()) + DELAY_START_TIME
            );
            taskTriggerRepo.save(taskTrigger);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void startTrigger(List<Long> triggerIdList) {
        triggerIdList.forEach(this::startTrigger);
    }

    @Override
    public void stopTrigger(Long triggerId) {
        taskTriggerRepo.findById(triggerId).ifPresent(taskTrigger -> {
            taskTrigger.setStatus(TaskTrigger.TriggerStatus.STOP);
            taskTrigger.setNextTime(0L);
            taskTriggerRepo.save(taskTrigger);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void stopTrigger(List<Long> triggerIdList) {
        triggerIdList.forEach(this::stopTrigger);
    }

    @Override
    public List<TaskTrigger> getDeadlineTrigger(long deadline, int size) {
        Page<TaskTrigger> triggerPage = taskTriggerRepo.findAllByStatusAndNextTimeIsLessThanEqual(
                TaskTrigger.TriggerStatus.RUNNING,
                deadline,
                PageRequest.of(0, size)
        );
        return triggerPage.getContent();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshTriggerTime(List<TaskTrigger> triggerList) {
        triggerList.forEach(i -> {
            long nextTime = i.getNextTime();
            i.setLastTime(nextTime);
            i.setNextTime(CronExpUtil.getNextTime(i.getCronExpression(), new Date(nextTime)));
        });
        taskTriggerRepo.saveAll(triggerList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addTimeoutTask(TaskScheduler taskScheduler, long deadline, int size) {
        lockService.lock(LOCK_NAME);
        boolean checkSuccess = false;
        List<TaskTrigger> taskTriggerList = getDeadlineTrigger(deadline, size);
        if (!CollectionUtils.isEmpty(taskTriggerList) && taskScheduler.isAvailable()) {
            for (TaskTrigger trigger : taskTriggerList) {
                RemoteTask task = new RemoteTask(trigger);
                taskScheduler.addTask(task);
                preConnect(taskScheduler, task.getPointSet());
            }
            refreshTriggerTime(taskTriggerList);
            checkSuccess = true;
        }
        return checkSuccess;
    }

    private void preConnect(TaskScheduler scheduler, Set<Point> pointSet) {
        for (Point point : pointSet) {
            scheduler.registerInvocation(point, new TaskInvocation(point));
        }
    }
}

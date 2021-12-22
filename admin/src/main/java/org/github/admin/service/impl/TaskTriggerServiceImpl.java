package org.github.admin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskInfo;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.task.RemoteTask;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.repo.TaskLockRepo;
import org.github.admin.repo.TaskTriggerRepo;
import org.github.admin.model.req.CreateTriggerReq;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    private TaskLockRepo taskLockRepo;

    private static final long PRE_READ_TIME = 5000L;

    private static final int PRE_READ_SIZE = 1000;

    private static final String LOCK_NAME = "task_lock";

    @Override
    public Page<TaskTrigger> list() {
        return taskTriggerRepo.findAll(PageRequest.of(0, 10));
    }

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
                    new Date()) + 5000L
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
    public List<TaskTrigger> getDeadlineTrigger(Long maxTime, Integer size) {
        Page<TaskTrigger> triggerPage = taskTriggerRepo.findAllByStatusAndNextTimeIsLessThanEqual(
                TaskTrigger.TriggerStatus.RUNNING,
                System.currentTimeMillis() + maxTime,
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
    public boolean checkTimeout(TaskScheduler taskScheduler) {
        lock();
        boolean checkSuccess = false;
        List<TaskTrigger> taskTriggerList = getDeadlineTrigger(PRE_READ_TIME, PRE_READ_SIZE);
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

    private void lock() {
        taskLockRepo.findByLockName(LOCK_NAME);
    }
}

package org.github.admin.service.impl;

import org.github.admin.model.entity.TaskInfo;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.repo.TaskTriggerRepo;
import org.github.admin.model.req.CreateTriggerReq;
import org.github.admin.service.TaskScheduler;
import org.github.admin.service.TaskTriggerService;
import org.github.admin.util.CronExpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Service
public class TaskTriggerServiceImpl implements TaskTriggerService {

    @Autowired
    private TaskTriggerRepo taskTriggerRepo;

    @Autowired
    private TaskInfoRepo taskInfoRepo;

    @Override
    public Page<TaskTrigger> list() {
        return taskTriggerRepo.findAll(PageRequest.of(0, 10));
    }

    @Override
    public void create(CreateTriggerReq req) {
        taskInfoRepo.findById(req.getTaskId()).ifPresent(new Consumer<TaskInfo>() {
            @Override
            public void accept(TaskInfo taskInfo) {
                TaskTrigger taskTrigger = new TaskTrigger();
                taskTrigger.setParameters(req.getParameters());
                taskTrigger.setCronExpression(req.getCronExpression());
                taskTrigger.setTaskInfo(taskInfo);
                taskInfo.getTriggerSet().add(taskTrigger);
                taskInfoRepo.save(taskInfo);
            }
        });
    }

    @Override
    public void startTrigger(Long triggerId) {
        taskTriggerRepo.findById(triggerId).ifPresent(new Consumer<TaskTrigger>() {
            @Override
            public void accept(TaskTrigger taskTrigger) {
                taskTrigger.setStatus(TaskTrigger.TriggerStatus.RUNNING);
                taskTrigger.setStartTime(System.currentTimeMillis());
                taskTrigger.setLastTime(taskTrigger.getNextTime());
                taskTrigger.setNextTime(CronExpUtil.getNextTime(
                        taskTrigger.getCronExpression(),
                        new Date()) + TaskScheduler.PRE_READ_TIME
                );
                taskTriggerRepo.save(taskTrigger);
            }
        });
    }

    @Override
    public void startTrigger(List<Long> triggerIdList) {
        triggerIdList.forEach(this::startTrigger);
    }

    @Override
    public void stopTrigger(Long triggerId) {
        taskTriggerRepo.findById(triggerId).ifPresent(new Consumer<TaskTrigger>() {
            @Override
            public void accept(TaskTrigger taskTrigger) {
                taskTrigger.setStatus(TaskTrigger.TriggerStatus.STOP);
                taskTrigger.setNextTime(0L);
                taskTriggerRepo.save(taskTrigger);
            }
        });
    }

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

    @Override
    public void refreshTriggerTime(List<TaskTrigger> triggerList) {
        triggerList.forEach(i -> {
            long nextTime = i.getNextTime();
            i.setLastTime(nextTime);
            i.setNextTime(CronExpUtil.getNextTime(i.getCronExpression(), new Date(nextTime)));
        });
        taskTriggerRepo.saveAll(triggerList);
    }
}

package org.github.admin.service.impl;

import org.github.admin.entity.TaskInfo;
import org.github.admin.entity.TaskTrigger;
import org.github.admin.entity.TriggerStatus;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.repo.TaskTriggerRepo;
import org.github.admin.req.CreateTriggerReq;
import org.github.admin.service.TaskTriggerService;
import org.github.admin.util.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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
                taskTrigger.setStatus(TriggerStatus.RUNNING);
                taskTrigger.setStartTime(System.currentTimeMillis());
                taskTrigger.setLastTime(taskTrigger.getNextTime());
                taskTrigger.setNextTime(getNextTime(taskTrigger.getCronExpression(), new Date()) + 5000);
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
                taskTrigger.setStatus(TriggerStatus.STOP);
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
                TriggerStatus.RUNNING,
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
            i.setNextTime(getNextTime(i.getCronExpression(), new Date(nextTime)));
        });
        taskTriggerRepo.saveAll(triggerList);
    }


    private Long getNextTime(String cron, Date date) {
        try {
            return new CronExpression(cron).getNextValidTimeAfter(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}

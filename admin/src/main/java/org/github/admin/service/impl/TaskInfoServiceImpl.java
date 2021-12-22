package org.github.admin.service.impl;

import org.github.admin.model.entity.TaskInfo;
import org.github.admin.model.entity.TaskGroup;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.model.req.AddTaskInfoReq;
import org.github.admin.service.TaskInfoService;
import org.github.common.TaskDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Service
public class TaskInfoServiceImpl implements TaskInfoService {

    @Autowired
    private TaskInfoRepo taskInfoRepo;

    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Override
    public Page<TaskInfo> list() {
        return taskInfoRepo.findAll(PageRequest.of(0, 10));
    }

    @Override
    public void addTask(AddTaskInfoReq req) {
        taskGroupRepo.findById(req.getTaskGroupId()).ifPresent(taskGroup -> {
            TaskInfo taskInfo = new TaskInfo();
            TaskDesc taskDesc = taskInfo.getTaskDesc();
            taskDesc.setTaskName(req.getTaskName());
            taskDesc.setClassName(req.getClassName());
            taskDesc.setMethodName(req.getMethodName());
            taskDesc.setParameterTypes(req.getParameterTypes());
            taskInfo.setTaskGroup(taskGroup);
            taskGroup.getTaskInfoSet().add(taskInfo);
            taskGroupRepo.save(taskGroup);
        });
    }
}

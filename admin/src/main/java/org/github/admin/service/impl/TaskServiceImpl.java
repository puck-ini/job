package org.github.admin.service.impl;

import org.github.admin.entity.Task;
import org.github.admin.entity.TaskGroup;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.repo.TaskRepo;
import org.github.admin.req.AddTaskReq;
import org.github.admin.service.TaskService;
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
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Override
    public Page<Task> list() {
        return taskRepo.findAll(PageRequest.of(0, 10));
    }

    @Override
    public void addTask(AddTaskReq req) {
        taskGroupRepo.findById(req.getTaskGroupId()).ifPresent(new Consumer<TaskGroup>() {
            @Override
            public void accept(TaskGroup taskGroup) {
                Task task = new Task();
                task.setTaskName(req.getTaskName());
                task.setClassName(req.getClassName());
                task.setMethodName(req.getMethodName());
                task.setParameterTypes(req.getParameterTypes());
                task.setTaskGroup(taskGroup);
                taskGroup.getTaskList().add(task);
                taskGroupRepo.save(taskGroup);
            }
        });
    }
}

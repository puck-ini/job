package org.github.admin.service.impl;

import org.checkerframework.checker.units.qual.C;
import org.github.admin.entity.Point;
import org.github.admin.entity.Task;
import org.github.admin.entity.TaskGroup;
import org.github.admin.req.AddTaskReq;
import org.github.admin.req.CreateGroupReq;
import org.github.admin.req.CreateTriggerReq;
import org.github.admin.service.TaskGroupService;
import org.github.admin.service.TaskService;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class TaskGroupServiceImplTest {

    @Autowired
    TaskGroupService taskGroupService;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskTriggerService taskTriggerService;

    @Test
    void addGroup() {
        CreateGroupReq req = new CreateGroupReq();
        Point point = new Point();
        point.setIp("127.0.0.1");
        point.setPort(9003);
        req.setName("test1");
        req.getPointList().add(point);
        taskGroupService.createGroup(req);
    }


    @Test
    void addTask() {
        for (TaskGroup group : taskGroupService.list().getContent()) {
            AddTaskReq req = new AddTaskReq();
            req.setClassName("testtask");
            req.setMethodName("testtask");
            req.setParameterTypes("testtask");
            req.setTaskName("testtask");
            req.setTaskGroupId(group.getId());
            taskService.addTask(req);
        }
    }

    @Test
    void addTrigger() {
        for (Task task : taskService.list().getContent()) {
            CreateTriggerReq req = new CreateTriggerReq();
            req.setParameters("test");
            req.setCronExpression("0/1 * * * * ? ");
            req.setTaskId(task.getId());
            taskTriggerService.create(req);
        }
    }
}
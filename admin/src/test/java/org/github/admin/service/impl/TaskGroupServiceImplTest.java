package org.github.admin.service.impl;

import org.github.admin.entity.Point;
import org.github.admin.entity.TaskInfo;
import org.github.admin.entity.TaskGroup;
import org.github.admin.req.AddTaskInfoReq;
import org.github.admin.req.CreateGroupReq;
import org.github.admin.req.CreateTriggerReq;
import org.github.admin.service.TaskGroupService;
import org.github.admin.service.TaskInfoService;
import org.github.admin.service.TaskTriggerService;
import org.github.common.TaskAppInfo;
import org.github.common.TaskDesc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TaskGroupServiceImplTest {

    @Autowired
    TaskGroupService taskGroupService;

    @Autowired
    TaskInfoService taskInfoService;

    @Autowired
    TaskTriggerService taskTriggerService;


    @Test
    void addTaskAppInfo() {
        TaskAppInfo taskAppInfo = new TaskAppInfo();
        taskAppInfo.setAppName("test123");
        taskAppInfo.setIp("127.0.0.1");
        taskAppInfo.setPort(39900);
        taskAppInfo.getTaskDescList().add(TaskDesc.builder().className("").methodName("").parameterTypes("").taskName("").build());
        taskGroupService.addGroup(taskAppInfo);
    }

    @Test
    void addGroup() {
        CreateGroupReq req = new CreateGroupReq();
        Point point = new Point();
        point.setIp("127.0.0.1");
        point.setPort(30003);
        req.setName("test1");
        req.getPointSet().add(point);
        taskGroupService.createGroup(req);
    }


    @Test
    void addTask() {
        for (TaskGroup group : taskGroupService.list().getContent()) {
            AddTaskInfoReq req = new AddTaskInfoReq();
            req.setClassName("org.github.tasktest.LogTask");
            req.setMethodName("log");
            req.setParameterTypes(null);
            req.setTaskName("log");
            req.setTaskGroupId(group.getId());
            taskInfoService.addTask(req);
        }
    }

    @Test
    void addTrigger() {
        for (TaskInfo taskInfo : taskInfoService.list().getContent()) {
            CreateTriggerReq req = new CreateTriggerReq();
            req.setParameters(null);
            req.setCronExpression("0/1 * * * * ? ");
            req.setTaskId(taskInfo.getId());
            taskTriggerService.create(req);
        }
    }
}
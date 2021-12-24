package org.github.admin.service.impl;

import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskInfo;
import org.github.admin.model.entity.TaskGroup;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.req.AddTaskInfoReq;
import org.github.admin.model.req.CreateGroupReq;
import org.github.admin.model.req.CreateTriggerReq;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.repo.TaskInfoRepo;
import org.github.admin.repo.TaskTriggerRepo;
import org.github.admin.service.TaskGroupService;
import org.github.admin.service.TaskInfoService;
import org.github.admin.service.TaskTriggerService;
import org.github.common.TaskAppInfo;
import org.github.common.TaskDesc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;


@SpringBootTest
class TaskGroupServiceImplTest {

    @Autowired
    TaskGroupService taskGroupService;

    @Autowired
    TaskInfoService taskInfoService;

    @Autowired
    TaskTriggerService taskTriggerService;


    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Autowired
    private TaskInfoRepo taskInfoRepo;

    @Autowired
    private TaskTriggerRepo taskTriggerRepo;


    @Test
    void addTaskAppInfo() {
        TaskAppInfo taskAppInfo = new TaskAppInfo();
        taskAppInfo.setAppName("test1");
        taskAppInfo.setIp("127.0.0.1");
        taskAppInfo.setPort(30003);
        taskAppInfo.getTaskDescList().add(TaskDesc.builder().className("org.github.tasktest.LogTask").methodName("log").parameterTypes("[]").taskName("log").build());
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
            req.setParameterTypes("[]");
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


    @DisplayName("对每个任务生成多个相同的触发器")
    @Test
    void test1() {
        for (TaskInfo taskInfo : taskInfoService.list().getContent()) {
            IntStream.range(0, 1000).forEach(i -> {
                CreateTriggerReq req = new CreateTriggerReq();
                req.setParameters(null);
                req.setCronExpression("0/1 * * * * ? ");
                req.setTaskId(taskInfo.getId());
                taskTriggerService.create(req);
            });
        }
    }

    @Test
    void deleteGroup() {
        taskGroupRepo.deleteAll();
    }


    @Test
    void deleteInfo() {
        taskInfoRepo.deleteAll();
    }

    @Test
    void deleteTrigger() {
        taskTriggerRepo.deleteAll();
//        List<TaskTrigger> taskTriggerList = taskTriggerRepo.findAll();
//        taskTriggerRepo.deleteAll(taskTriggerList);
    }
}
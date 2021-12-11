package org.github.admin.repo;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.entity.Point;
import org.github.admin.entity.TaskInfo;
import org.github.admin.entity.TaskGroup;
import org.github.admin.entity.TaskTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
@SpringBootTest
public class RepoTests {

    @Autowired
    private TaskInfoRepo taskInfoRepo;

    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Autowired
    private TaskTriggerRepo taskTriggerRepo;


    @BeforeEach
    @Test
    void init() {
        TaskGroup taskGroup = new TaskGroup();
        List<TaskInfo> taskInfoList = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String s = String.valueOf(i);
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setClassName(s);
            taskInfo.setTaskName(s);
            taskInfo.setMethodName(s);
            taskInfo.setParameterTypes(s);
            taskInfo.setTaskGroup(taskGroup);
            taskInfoList.add(taskInfo);
            Point point = new Point();
            point.setIp(s);
            point.setPort(i);
            point.setTaskGroup(taskGroup);
            pointList.add(point);
            List<TaskTrigger> taskTriggerList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                String s1 = String.valueOf(j);
                TaskTrigger taskTrigger = new TaskTrigger();
                taskTrigger.setCronExpression(s1);
                taskTrigger.setTaskInfo(taskInfo);
                taskTriggerList.add(taskTrigger);
            }
            taskInfo.setTriggerList(taskTriggerList);
        }
        taskGroup.setPointList(pointList);
        taskGroup.setTaskInfoList(taskInfoList);
        taskGroupRepo.save(taskGroup);
    }

    @Test
    void listGroup() {
        List<TaskGroup> taskGroupList = taskGroupRepo.findAll();
        for (TaskGroup group : taskGroupList) {
            log.info(group.toString());
            for (TaskInfo taskInfo : group.getTaskInfoList()) {
                log.info(taskInfo.toString());
                for (TaskTrigger taskTrigger : taskInfo.getTriggerList()) {
                    log.info(taskTrigger.toString());
                }
            }
            for (Point point : group.getPointList()) {
                log.info(point.toString());
            }
        }
    }

    // 不能通过 many 添加
    @Test
    void addTask() {
        List<TaskGroup> taskGroupList = taskGroupRepo.findAll();
        for (TaskGroup group : taskGroupList) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setTaskName("test");
            taskInfo.setMethodName("test");
            taskInfo.setTaskGroup(group);
            taskInfoRepo.save(taskInfo);
        }
        this.listGroup();
    }

    @BeforeEach
    @Test
    void delete() {
        taskGroupRepo.deleteAll();
    }
}

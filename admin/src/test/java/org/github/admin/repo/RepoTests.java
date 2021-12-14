package org.github.admin.repo;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.entity.Point;
import org.github.admin.entity.TaskInfo;
import org.github.admin.entity.TaskGroup;
import org.github.admin.entity.TaskTrigger;
import org.github.common.TaskDesc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<TaskInfo> taskInfoList = new HashSet<>();
        Set<Point> pointList = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String s = String.valueOf(i);
            TaskInfo taskInfo = new TaskInfo();
            TaskDesc taskDesc = taskInfo.getTaskDesc();
            taskDesc.setClassName(s);
            taskDesc.setTaskName(s);
            taskDesc.setMethodName(s);
            taskDesc.setParameterTypes(s);
            taskInfo.setTaskGroup(taskGroup);
            taskInfoList.add(taskInfo);
            Point point = new Point();
            point.setIp(s);
            point.setPort(i);
            point.setTaskGroup(taskGroup);
            pointList.add(point);
            Set<TaskTrigger> taskTriggerList = new HashSet<>();
            for (int j = 0; j < 10; j++) {
                String s1 = String.valueOf(j);
                TaskTrigger taskTrigger = new TaskTrigger();
                taskTrigger.setCronExpression(s1);
                taskTrigger.setTaskInfo(taskInfo);
                taskTriggerList.add(taskTrigger);
            }
            taskInfo.setTriggerSet(taskTriggerList);
        }
        taskGroup.setPointSet(pointList);
        taskGroup.setTaskInfoSet(taskInfoList);
        taskGroupRepo.save(taskGroup);
    }

    @Test
    void listGroup() {
        List<TaskGroup> taskGroupList = taskGroupRepo.findAll();
        for (TaskGroup group : taskGroupList) {
            log.info(group.toString());
            for (TaskInfo taskInfo : group.getTaskInfoSet()) {
                log.info(taskInfo.toString());
                for (TaskTrigger taskTrigger : taskInfo.getTriggerSet()) {
                    log.info(taskTrigger.toString());
                }
            }
            for (Point point : group.getPointSet()) {
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
            TaskDesc taskDesc = taskInfo.getTaskDesc();
            taskDesc.setTaskName("test");
            taskDesc.setMethodName("test");
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

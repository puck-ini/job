package org.github.admin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskGroup;
import org.github.admin.model.entity.TaskInfo;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.model.req.CreateGroupReq;
import org.github.admin.service.TaskGroupService;
import org.github.common.TaskAppInfo;
import org.github.common.TaskDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Slf4j
@Service
public class TaskGroupServiceImpl implements TaskGroupService {

    @Autowired
    private TaskGroupRepo taskGroupRepo;


    @Override
    public Page<TaskGroup> list() {
        return taskGroupRepo.findAll(PageRequest.of(0, 10));
    }

    @Override
    public void createGroup(CreateGroupReq req) {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setName(req.getName());
        taskGroup.setPointSet(req.getPointSet());
        for (Point point : req.getPointSet()) {
            point.setTaskGroup(taskGroup);
        }
        taskGroupRepo.save(taskGroup);
    }

    @Override
    public void addGroup(TaskAppInfo info) {
        TaskGroup taskGroup = taskGroupRepo.findByName(info.getAppName());
        if (Objects.isNull(taskGroup)) {
            taskGroup = new TaskGroup();
        }
        taskGroup.setName(info.getAppName());
        Point point = new Point(info.getIp(), info.getPort(), taskGroup);
        taskGroup.getPointSet().add(point);
        Set<TaskInfo> taskInfoList = taskGroup.getTaskInfoSet();
        for (TaskDesc desc : info.getTaskDescList()) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setTaskDesc(desc);
            taskInfo.setTaskGroup(taskGroup);
            taskInfoList.add(taskInfo);
        }
        taskGroupRepo.save(taskGroup);
    }
}

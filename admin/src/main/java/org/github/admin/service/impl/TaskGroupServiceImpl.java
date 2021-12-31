package org.github.admin.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.model.entity.TaskTrigger;
import org.github.common.req.TaskMethod;
import org.github.common.types.Point;
import org.github.admin.model.entity.TaskGroup;
import org.github.admin.model.entity.TaskInfo;
import org.github.admin.repo.TaskGroupRepo;
import org.github.common.req.CreateGroupReq;
import org.github.admin.service.TaskGroupService;
import org.github.common.req.TaskAppInfo;
import org.github.common.types.TaskDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String NULL_PARAMETER_TYPES = JSON.toJSONString(new Object[]{});

    @Override
    public Page<TaskGroup> list() {
        return taskGroupRepo.findAll(PageRequest.of(0, 10));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createGroup(CreateGroupReq req) {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setName(req.getName());
        taskGroup.setPointSet(req.getPointSet());
        taskGroupRepo.save(taskGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addGroup(TaskAppInfo info) {
        TaskGroup taskGroup = taskGroupRepo.findByName(info.getAppName()).orElse(new TaskGroup());
        taskGroup.setName(info.getAppName());
        Point point = new Point(info.getIp(), info.getPort());
        taskGroup.getPointSet().add(point);
        Set<TaskInfo> taskInfoList = taskGroup.getTaskInfoSet();
        for (TaskMethod taskMethod : info.getTaskMethodList()) {
            TaskInfo taskInfo = new TaskInfo();
            TaskDesc desc = taskMethod.getTaskDesc();
            taskInfo.setTaskDesc(desc);
            taskInfo.setTaskGroup(taskGroup);
            taskInfoList.add(taskInfo);
            if (Objects.equals(desc.getParameterTypes(), NULL_PARAMETER_TYPES) && Objects.nonNull(taskMethod.getCron())) {
                TaskTrigger trigger = new TaskTrigger();
                trigger.setCronExpression(taskMethod.getCron());
                trigger.setTaskInfo(taskInfo);
                taskInfo.getTriggerSet().add(trigger);
            }
        }
        taskGroupRepo.save(taskGroup);
    }
}

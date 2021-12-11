package org.github.admin.service.impl;

import org.github.admin.entity.Point;
import org.github.admin.entity.TaskGroup;
import org.github.admin.repo.TaskGroupRepo;
import org.github.admin.req.CreateGroupReq;
import org.github.admin.service.TaskGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

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
        taskGroup.setPointList(req.getPointList());
        for (Point point : req.getPointList()) {
            point.setTaskGroup(taskGroup);
        }
        taskGroupRepo.save(taskGroup);
    }
}

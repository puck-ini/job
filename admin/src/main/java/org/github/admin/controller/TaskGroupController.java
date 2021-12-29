package org.github.admin.controller;

import org.github.common.types.Point;
import org.github.admin.model.entity.TaskGroup;
import org.github.common.req.CreateGroupReq;
import org.github.admin.scheduler.SchedulerService;
import org.github.admin.scheduler.TaskInvocation;
import org.github.admin.service.TaskGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

@RestController
@RequestMapping("/group")
public class TaskGroupController {

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private SchedulerService schedulerService;

    @GetMapping("/list")
    public List<TaskGroup> list() {
        return taskGroupService.list().getContent();
    }

    @PostMapping
    public void add(@RequestBody CreateGroupReq req) {
        taskGroupService.createGroup(req);
        for (Point point : req.getPointSet()) {
            schedulerService.register(point, new TaskInvocation(point));
        }
    }
}

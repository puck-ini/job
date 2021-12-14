package org.github.admin.controller;

import org.github.admin.entity.BaseEntity;
import org.github.admin.entity.TaskTrigger;
import org.github.admin.req.CreateTriggerReq;
import org.github.admin.service.TaskTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

@RestController
@RequestMapping("/trigger")
public class TaskTriggerController {

    @Autowired
    private TaskTriggerService taskTriggerService;

    @PostMapping("/create")
    public void create(CreateTriggerReq req) {
        taskTriggerService.create(req);
    }

    @GetMapping("/list")
    public List<TaskTrigger> list() {
        return taskTriggerService.list().getContent();
    }


    @GetMapping("/start/{id}")
    public void start(@PathVariable("id") Long id) {
        taskTriggerService.startTrigger(id);
    }

    @GetMapping("/start/all")
    public void startAll() {
        taskTriggerService.startTrigger(taskTriggerService.list().getContent().stream().map(BaseEntity::getId).collect(Collectors.toList()));
    }

    @GetMapping("/stop/{id}")
    public void stop(@PathVariable("id") Long id) {
        taskTriggerService.stopTrigger(id);
    }

    @GetMapping("/stop/all")
    public void stopAll() {
        taskTriggerService.stopTrigger(taskTriggerService.list().getContent().stream().map(BaseEntity::getId).collect(Collectors.toList()));
    }
}

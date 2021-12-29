package org.github.admin.controller;

import org.github.admin.model.entity.TaskTrigger;
import org.github.common.req.CreateTriggerReq;
import org.github.admin.service.TaskTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void create(@RequestBody CreateTriggerReq req) {
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
        taskTriggerService.startAll();
    }

    @GetMapping("/stop/{id}")
    public void stop(@PathVariable("id") Long id) {
        taskTriggerService.stopTrigger(id);
    }

    @GetMapping("/stop/all")
    public void stopAll() {
        taskTriggerService.stopAll();
    }
}

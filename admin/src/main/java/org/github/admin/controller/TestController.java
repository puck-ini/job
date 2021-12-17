package org.github.admin.controller;

import org.github.admin.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zengchzh
 * @date 2021/12/16
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SchedulerService schedulerService;

    @GetMapping("/add")
    public void add() {
        schedulerService.addCheckThread();
    }


    @GetMapping("/stop")
    public void stop() {
        schedulerService.stop();
    }
}

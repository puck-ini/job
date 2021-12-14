package org.github.admin.controller;

import org.github.admin.entity.TaskGroup;
import org.github.admin.service.TaskGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/list")
    public List<TaskGroup> list() {
        return taskGroupService.list().getContent();
    }
}

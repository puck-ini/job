package org.github.admin.controller;

import org.github.admin.entity.TaskInfo;
import org.github.admin.service.TaskInfoService;
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
@RequestMapping("/info")
public class TaskInfoController {

    @Autowired
    private TaskInfoService taskInfoService;

    @GetMapping("/list")
    public List<TaskInfo> list() {
        return taskInfoService.list().getContent();
    }
}

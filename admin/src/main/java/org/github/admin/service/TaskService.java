package org.github.admin.service;

import org.github.admin.entity.Task;
import org.github.admin.req.AddTaskReq;
import org.springframework.data.domain.Page;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskService {

    Page<Task> list();

    void addTask(AddTaskReq req);

}

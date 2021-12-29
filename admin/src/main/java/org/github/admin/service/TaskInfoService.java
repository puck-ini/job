package org.github.admin.service;

import org.github.admin.model.entity.TaskInfo;
import org.github.common.req.AddTaskInfoReq;
import org.springframework.data.domain.Page;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskInfoService {

    Page<TaskInfo> list();

    void addTask(AddTaskInfoReq req);

}

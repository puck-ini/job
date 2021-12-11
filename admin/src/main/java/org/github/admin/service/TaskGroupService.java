package org.github.admin.service;

import org.github.admin.entity.TaskGroup;
import org.github.admin.req.CreateGroupReq;
import org.springframework.data.domain.Page;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskGroupService {

    Page<TaskGroup> list();

    void createGroup(CreateGroupReq req);
}

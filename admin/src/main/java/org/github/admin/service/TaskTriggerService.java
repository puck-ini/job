package org.github.admin.service;

import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.req.CreateTriggerReq;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskTriggerService {

    Page<TaskTrigger> list();

    void create(CreateTriggerReq req);

    void startTrigger(Long triggerId);

    void startTrigger(List<Long> triggerIdList);

    void stopTrigger(Long triggerId);

    void stopTrigger(List<Long> triggerIdList);

    List<TaskTrigger> getDeadlineTrigger(Long maxTime, Integer size);

    void refreshTriggerTime(List<TaskTrigger> triggerList);
}

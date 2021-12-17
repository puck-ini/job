package org.github.admin.scheduler;

import org.github.admin.model.task.TimerTask;
import org.github.common.TaskReq;

/**
 * @author zengchzh
 * @date 2021/12/16
 */
public interface Invocation {

    void connnect();

    void invoke(TaskReq req);

    void disconnect();
}
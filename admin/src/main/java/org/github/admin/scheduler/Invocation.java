package org.github.admin.scheduler;

import org.github.admin.model.entity.Point;
import org.github.common.protocol.TaskReq;

/**
 * @author zengchzh
 * @date 2021/12/16
 */
public interface Invocation {

    void connnect();

    void invoke(TaskReq req);

    void disconnect();

    boolean isAvailable();

    Point getPoint();
}

package org.github.admin.model.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.convert.RemoteTaskConvert;
import org.github.admin.scheduler.Invocation;
import org.github.common.protocol.TaskReq;

import java.util.*;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

@Slf4j
@Data
public class RemoteTask implements TimerTask {
    /**
     * 任务名
     */
    private String taskName;

    /**
     * 任务所在的类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 任务参数类型，Class<?>[] json
     */
    private String parameterTypes;
    /**
     * 任务参数，Object[] json
     */
    private String parameters;
    /**
     * cron 表达式
     */
    private String cronExpression;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 上次运行时间
     */
    private Long lastTime;

    /**
     * 下次运行时间
     */
    private Long nextTime;

    private List<Invocation> invocationList;

    public RemoteTask(List<Invocation> invocationList) {
        this.invocationList = invocationList;
    }

    @Override
    public void run() {
        if (Objects.isNull(invocationList) || invocationList.isEmpty()) {
            log.error("invocation list is empty");
            return;
        }
        List<Invocation> availableList = new ArrayList<>();
        for (Invocation invocation : invocationList) {
            if (invocation.isAvailable()) {
                availableList.add(invocation);
            } else {
                log.error("task [" + taskName + "] : " + invocation.getPoint() + " unavailable ");
            }
        }
        if (!availableList.isEmpty()) {
            TaskReq req = RemoteTaskConvert.convertToTaskReq(this);
            Invocation invocation = availableList.get(new Random().nextInt(availableList.size()));
            invocation.invoke(req);
        } else {
            log.error("task [" + taskName + "] run fail");
        }
    }

    @Override
    public void refresh() {

    }

    @Override
    public String getName() {
        return this.taskName;
    }

    @Override
    public long getNextTime() {
        return this.nextTime;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCancel() {
        return true;
    }
}

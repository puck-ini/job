package org.github.admin.model.task;

import lombok.Data;
import org.github.admin.model.entity.Point;
import org.github.admin.model.entity.TaskTrigger;
import org.github.common.TaskDesc;

import java.util.Set;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

@Data
public class RemoteTask implements TimerTask{

    private Set<Point> pointSet;
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

    public RemoteTask(TaskTrigger trigger) {
        this.pointSet = trigger.getTaskInfo().getTaskGroup().getPointSet();
        TaskDesc desc = trigger.getTaskInfo().getTaskDesc();
        this.taskName = desc.getTaskName();
        this.className = desc.getClassName();
        this.methodName = desc.getMethodName();
        this.parameterTypes = desc.getParameterTypes();
        this.parameters = trigger.getParameters();
        this.cronExpression = trigger.getCronExpression();
        this.startTime = trigger.getStartTime();
        this.lastTime = trigger.getLastTime();
        this.nextTime = trigger.getNextTime();
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
        return false;
    }
}

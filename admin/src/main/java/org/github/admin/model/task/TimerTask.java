package org.github.admin.model.task;

/**
 * @author zengchzh
 * @date 2021/12/14
 */
public interface TimerTask {

    String getName();

    long getNextTime();

    void cancel();

    boolean isCancel();
}

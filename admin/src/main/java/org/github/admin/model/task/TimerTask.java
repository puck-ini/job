package org.github.admin.model.task;

/**
 * @author zengchzh
 * @date 2021/12/14
 */
public interface TimerTask {

    void run();

    void refresh();

    String getName();

    long getNextTime();

    default void cancel() {};

    default boolean isCancel() { return true; };
}

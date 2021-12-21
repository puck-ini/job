package org.github.admin.model.task;

import org.github.admin.util.CronExpUtil;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

public class LocalTask implements TimerTask {

    private final String taskName;

    private final Runnable r;

    private final String cron;

    /**
     * 下次运行时间
     */
    private long nextTime;

    private boolean cancel;

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    public LocalTask(Runnable r, String cron) {
        this(LocalTask.class.getSimpleName() + "-" + COUNT.getAndIncrement(), r, cron);
    }

    public LocalTask(String taskName, Runnable r, String cron) {
        this.taskName = taskName;
        this.r = r;
        this.cron = cron;
        this.cancel = false;
        refresh(new Date(System.currentTimeMillis() + 3000));
    }

    public void run() {
        this.r.run();
        refresh(new Date(this.nextTime));
    }

    private void refresh(Date date) {
        this.nextTime = CronExpUtil.getNextTime(cron, date);
    }

    @Override
    public String getName() {
        return this.taskName;
    }

    @Override
    public long getNextTime() {
        return nextTime;
    }

    @Override
    public void cancel() {
        this.cancel = true;
    }

    @Override
    public boolean isCancel() {
        return this.cancel;
    }
}

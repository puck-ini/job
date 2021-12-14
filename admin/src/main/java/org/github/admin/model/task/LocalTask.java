package org.github.admin.model.task;

import org.github.admin.util.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * @author zengchzh
 * @date 2021/12/14
 */

public class LocalTask implements TimerTask {


    private final Runnable r;

    private final String cron;

    /**
     * 下次运行时间
     */
    private long nextTime;

    public LocalTask(Runnable r, String cron) {
        this.r = r;
        this.cron = cron;
        refresh(new Date(System.currentTimeMillis() + 3000));
    }

    public void run() {
        this.r.run();
        refresh(new Date(this.nextTime));
    }

    private void refresh(Date date) {
        try {
            this.nextTime = new CronExpression(cron).getNextValidTimeAfter(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public long getNextTime() {
        return nextTime;
    }
}

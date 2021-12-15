package org.github.admin.util;

import java.text.ParseException;
import java.util.Date;

/**
 * @author zengchzh
 * @date 2021/12/15
 */
public class CronExpUtil {


    public static long getNextTime(String cron, Date date) {
        try {
            return new CronExpression(cron).getNextValidTimeAfter(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}

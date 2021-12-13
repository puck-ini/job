package org.github.tasktest.test;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.Task;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Slf4j
public class TaskTest3 {

    @Task
    public void taskTest3() {
        log.info("test1");
    }
}

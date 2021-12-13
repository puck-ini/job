package org.github.tasktest.test;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.Task;
import org.springframework.stereotype.Component;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Component
@Slf4j
public class TaskTest2 {

    public void test1() {
        log.info("test1");
    }

    @Task
    public void test2() {
        log.info("test2");
    }
}

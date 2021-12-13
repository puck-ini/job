package org.github.tasktest.test;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.Task;
import org.springframework.stereotype.Component;

/**
 * @author zengchzh
 * @date 2021/12/11
 */
@Component
@Task
@Slf4j
public class TaskTest1 {


    public void taskTest1() {
      log.info("hello world");
    }
}

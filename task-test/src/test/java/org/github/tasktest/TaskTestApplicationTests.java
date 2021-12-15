package org.github.tasktest;

import lombok.extern.slf4j.Slf4j;
import org.github.taskstarter.TaskInfoHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
class TaskTestApplicationTests {

    @Test
    void logTaskInfo() {
        log.info(TaskInfoHolder.getTaskInfo().toString());
    }


}

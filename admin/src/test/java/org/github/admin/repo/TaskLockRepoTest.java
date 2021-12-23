package org.github.admin.repo;

import org.github.admin.service.impl.TaskLockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskLockRepoTest {

    @Autowired
    private TaskLockService lockService;

    @Autowired
    private TaskLockRepo taskLockRepo;

    private static final int NUM = 10;

    private CountDownLatch countDownLatch = new CountDownLatch(NUM);

    private static final String NAME = "test";

    @Test
    void test() throws InterruptedException {
        IntStream.range(0, NUM).forEach(i -> new Thread(() -> {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lockService.lock(NAME);
        }).start());

        Thread.sleep(2000);
    }



    @AfterEach
    void deleteLock() {
        taskLockRepo.deleteById(NAME);
    }
}
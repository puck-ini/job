package org.github.admin.controller;

import org.github.common.types.Point;
import org.github.admin.model.entity.TaskInfo;
import org.github.common.req.CreateGroupReq;
import org.github.common.req.CreateTriggerReq;
import org.github.admin.service.TaskInfoService;
import org.github.admin.service.TaskTriggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    TaskInfoService taskInfoService;

    @Autowired
    TaskTriggerService taskTriggerService;


    private static final String URL = "http://127.0.0.1:";

    @Test
    void testRun() {
        addGroup();
        sleep(5);
//        addTrigger();
//        sleep(2);
        startTrigger();
        sleep(30);
        stopTrigger();
    }

    @Test
    void addGroup() {
        CreateGroupReq req = new CreateGroupReq();
        req.setName("test1");
        Set<Point> pointSet = new HashSet<>();
        pointSet.add(new Point("127.0.0.1", 30003));
        req.setPointSet(pointSet);
        HttpEntity<CreateGroupReq> entity = new HttpEntity<>(req);
        restTemplate.exchange(URL + port + "/task/group", HttpMethod.POST, entity, Void.class);
        sleep(5);
    }


    void addTrigger() {
        for (TaskInfo taskInfo : taskInfoService.list().getContent()) {
            CreateTriggerReq req = new CreateTriggerReq();
            req.setParameters(null);
            req.setCronExpression("0/1 * * * * ? ");
            req.setTaskId(taskInfo.getId());
            taskTriggerService.create(req);
        }
    }

    @Test
    void startTrigger() {
        restTemplate.getForEntity(URL + port + "/task/trigger/start/all", Void.class);
    }

    @Test
    void stopTrigger() {
        restTemplate.getForEntity(URL + port + "/task/trigger/stop/all", Void.class);
    }

    void sleep(int seconds) {

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
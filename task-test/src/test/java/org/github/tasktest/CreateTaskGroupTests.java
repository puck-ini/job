package org.github.tasktest;

import org.github.common.req.CreateGroupReq;
import org.github.common.types.Point;
import org.github.common.util.ServerUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zengchzh
 * @date 2022/2/18
 */
public class CreateTaskGroupTests {

    RestTemplate restTemplate = new RestTemplate();

    /**
     * 注册服务信息
     */
    @Test
    void createTaskGroup() {
        CreateGroupReq req = new CreateGroupReq();
        req.setName("test1");
        Set<Point> pointSet = new HashSet<>();
        // 端口查看 org.github.taskstarter.TaskProp
        pointSet.add(new Point(ServerUtil.getHost(), 30003));
        req.setPointSet(pointSet);
        restTemplate.postForEntity("http://localhost:9003/task/group", req, Void.class);
    }

    /**
     * 开启所有任务
     */
    @Test
    void startAll() {
        restTemplate.getForEntity("http://localhost:9003/task/trigger/start/all", Void.class);
    }

    /**
     * 暂停所有任务
     */
    @Test
    void stopAll() {
        restTemplate.getForEntity("http://localhost:9003/task/trigger/stop/all", Void.class);
    }
}

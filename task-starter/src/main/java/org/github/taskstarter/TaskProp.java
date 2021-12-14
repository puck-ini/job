package org.github.taskstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Data
@EnableConfigurationProperties(TaskProp.class)
@ConfigurationProperties("task")
public class TaskProp {

    private Task task = new Task();

    private String zkAddress = "127.0.0.1:2181";

    @Data
    static class Task {

        /**
         * 服务端暴露的端口
         */
        private int port = 30003;
        /**
         * 服务端地址
         */
        private String address = "localhost:30001";
    }
}

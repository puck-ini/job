package org.github.admin;

import org.github.common.ZkRegister;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.start();
        return taskScheduler;
    }

    @Value("${zk.address:127.0.0.1:2181}")
    private String zkAddress;

    @Bean
    public ZkRegister zkRegister() {
        return new ZkRegister(zkAddress);
    }

}

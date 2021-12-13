package org.github.admin;

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
//        taskScheduler.start();
        return taskScheduler;
    }

}

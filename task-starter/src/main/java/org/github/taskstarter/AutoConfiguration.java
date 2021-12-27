package org.github.taskstarter;

import org.github.common.ZkRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zengchzh
 * @date 2021/12/11
 */


@Configuration
public class AutoConfiguration {

    @Bean
    public TaskProp taskProp() {
        return new TaskProp();
    }

    @Bean
    public TaskInfoHolder taskInfoHolder() {
        return new TaskInfoHolder();
    }

    @Bean
    public ZkRegister zkRegister(@Autowired TaskProp taskProp) {
        return new ZkRegister(taskProp.getZkAddress());
    }

    @Bean
    public TaskListener taskListener(@Autowired TaskProp taskProp) {
        return new TaskListener(taskProp.getPort());
    }
}

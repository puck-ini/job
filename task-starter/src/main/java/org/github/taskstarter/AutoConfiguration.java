package org.github.taskstarter;

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
}

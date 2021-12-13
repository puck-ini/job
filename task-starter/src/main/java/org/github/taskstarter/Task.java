package org.github.taskstarter;

import java.lang.annotation.*;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Task {

    String taskName() default "";
}

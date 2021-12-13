package org.github.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDesc implements Serializable {

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 任务所在的类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 任务参数类型，Class<?>[] json
     */
    private String parameterTypes;
}

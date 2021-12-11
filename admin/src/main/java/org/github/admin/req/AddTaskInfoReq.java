package org.github.admin.req;

import lombok.Data;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Data
public class AddTaskInfoReq {

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

    private Long taskGroupId;
}

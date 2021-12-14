package org.github.admin.model.req;

import lombok.Data;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Data
public class CreateTriggerReq {


    /**
     * 任务参数，Object[] json
     */
    private String parameters;
    /**
     * cron 表达式
     */
    private String cronExpression;

    private Long taskId;

}

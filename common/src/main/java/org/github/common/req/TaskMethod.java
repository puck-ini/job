package org.github.common.req;

import lombok.Data;
import org.github.common.types.TaskDesc;

import java.io.Serializable;

/**
 * @author zengchzh
 * @date 2021/12/29
 */
@Data
public class TaskMethod implements Serializable {


    private TaskDesc taskDesc = new TaskDesc();

    private String cron;
}

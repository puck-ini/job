package org.github.common.req;

import lombok.Data;
import org.github.common.types.TaskDesc;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@Data
public class AddTaskInfoReq {

    private TaskDesc taskDesc = new TaskDesc();

    private Long taskGroupId;
}

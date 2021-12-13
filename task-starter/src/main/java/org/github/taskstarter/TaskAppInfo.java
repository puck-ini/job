package org.github.taskstarter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.github.common.TaskDesc;

import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAppInfo {

    private String appName;

    private String ip;

    private int port;

    private List<TaskDesc> taskDescList;
}

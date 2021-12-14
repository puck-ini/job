package org.github.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.github.common.TaskDesc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAppInfo implements Serializable {

    private String appName;

    private String ip;

    private int port;

    private List<TaskDesc> taskDescList = new ArrayList<>();

    @Override
    public TaskAppInfo clone() {
        return TaskAppInfo.builder().appName(appName).ip(ip).port(port).taskDescList(taskDescList).build();
    }
}

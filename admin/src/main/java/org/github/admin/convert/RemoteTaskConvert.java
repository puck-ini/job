package org.github.admin.convert;

import com.alibaba.fastjson.JSON;
import org.github.admin.model.entity.TaskTrigger;
import org.github.admin.model.task.RemoteTask;
import org.github.common.types.TaskDesc;
import org.github.common.protocol.TaskReq;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zengchzh
 * @date 2021/12/27
 */
public class RemoteTaskConvert {

    public static TaskReq convertToTaskReq(RemoteTask task) {
        return TaskReq.builder()
                .requestId(UUID.randomUUID().toString())
                .className(task.getClassName())
                .methodName(task.getMethodName())
                .parameterTypes(parseTypesJson(task.getParameterTypes()))
                .parameters(parseParaJson(task.getParameters()))
                .build();
    }



    private static Class[] parseTypesJson(String json) {
        if (Objects.isNull(json)) {
            return new Class[]{};
        }
        List<Class> objects = JSON.parseArray(json, Class.class);
        Class[] arr = new Class[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            arr[i] = objects.get(i);
        }
        return arr;
    }

    private static Object[] parseParaJson(String json) {
        if (Objects.isNull(json)) {
            return new Object[]{};
        }
        List<Object> objects = JSON.parseArray(json, Object.class);
        return objects.toArray();
    }
}

package org.github.taskstarter;

import com.alibaba.fastjson.JSON;
import org.github.common.ServerUtil;
import org.github.common.TaskDesc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zengchzh
 * @date 2021/12/11
 *
 */

public class TaskInfoHolder implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private TaskProp taskProp;

    @Value("${spring.application.name:null}")
    private String appName;

    private ApplicationContext context;

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (Objects.isNull(event.getApplicationContext().getParent())) {
            context = event.getApplicationContext();
            init();
            startServer();
        }
    }



    private void init() {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) {
                continue;
            }
            Task task = clazz.getAnnotation(Task.class);
            Object obj = context.getBean(name);
            if (Objects.nonNull(task)) {
                CACHE.put(clazz.getName(), obj);
            } else {
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    Task task1 = method.getAnnotation(Task.class);
                    if (Objects.nonNull(task1)) {
                        CACHE.put(clazz.getName(), obj);
                    }
                }
            }

        }
    }

    private void startServer() {
        new Thread(() -> {
            new TaskServer(taskProp.getTask().getPort()).start();
        }).start();
    }

    public static Object get(String key) {
        return CACHE.get(key);
    }

    public TaskAppInfo getTaskInfo() {
        String[] names = context.getBeanDefinitionNames();
        TaskAppInfo taskAppInfo = TaskAppInfo.builder()
                .appName(appName)
                .ip(ServerUtil.getHost())
                .port(taskProp.getTask().getPort())
                .build();
        List<TaskDesc> taskDescList = new ArrayList<>();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) {
                continue;
            }
            Task task = clazz.getAnnotation(Task.class);
            Method[] methods = clazz.getDeclaredMethods();
            String className = clazz.getName();
            if (Objects.nonNull(task)) {
                for (Method method : methods) {
                    if (!method.isBridge()) {
                        taskDescList.add(TaskDesc.builder()
                                .taskName(method.getName())
                                .className(className)
                                .methodName(method.getName())
                                .parameterTypes(JSON.toJSONString(method.getParameterTypes()))
                                .build());
                    }
                }
            } else {
                for (Method method : methods) {
                    Task task1 = method.getAnnotation(Task.class);
                    if (Objects.nonNull(task1)) {
                        taskDescList.add(TaskDesc.builder()
                                .taskName(StringUtils.isEmpty(task1.taskName()) ? method.getName() : task1.taskName())
                                .className(className)
                                .methodName(method.getName())
                                .parameterTypes(JSON.toJSONString(method.getParameterTypes()))
                                .build());
                    }
                }
            }
        }
        taskAppInfo.setTaskDescList(taskDescList);
        return taskAppInfo;
    }

}

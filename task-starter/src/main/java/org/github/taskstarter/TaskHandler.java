package org.github.taskstarter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.github.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class TaskHandler extends SimpleChannelInboundHandler<TaskMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TaskMsg msg) throws Exception {
        if (MsgType.PRE_REQ == msg.getMsgType()) {
            TaskAppInfo taskAppInfo = TaskInfoHolder.getTaskInfo();
            TaskMsg taskMsg = TaskMsg.builder()
                    .msgType(MsgType.PRE_RES)
                    .data(taskAppInfo)
                    .build();
            ctx.channel().writeAndFlush(taskMsg);
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            TaskReq req = (TaskReq) msg.getData();
            TaskRes res = new TaskRes();
            res.setRequestId(req.getRequestId());
            try {
                Object o = handler(req);
                res.setResult(o);
            } catch (Throwable t) {
                res.setError(t.toString());
            }
            return res;
        }).thenAccept(res -> {
            TaskMsg taskMsg = TaskMsg.builder().msgType(MsgType.RES).data(res).build();
            ctx.channel().writeAndFlush(taskMsg);
        });
    }


    private Object handler(TaskReq req) throws InvocationTargetException {
        String className = req.getClassName();
        Object obj = TaskInfoHolder.get(className);
        if (Objects.nonNull(obj)) {
            Class<?> serviceClass = obj.getClass();
            String methodName = req.getMethodName();
            Class<?>[] parameterTypes = req.getParameterTypes();
            Object[] parameters = req.getParameters();
            FastClass fastClass = FastClass.create(serviceClass);
            int methodIndex = fastClass.getIndex(methodName, parameterTypes);
            return fastClass.invoke(methodIndex, obj, parameters);
        }
        return null;
    }
}

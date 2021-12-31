package org.github.taskstarter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.github.common.coder.MsgDecoder;
import org.github.common.coder.MsgEncoder;
import org.github.common.protocol.MsgType;
import org.github.common.protocol.TaskMsg;
import org.github.common.protocol.TaskReq;
import org.github.common.protocol.TaskRes;
import org.github.common.req.TaskAppInfo;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.context.SmartLifecycle;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class TaskListener implements SmartLifecycle {

    private final ListenerThread listener;

    private boolean running;

    public TaskListener(int port) {
        listener = new ListenerThread(port);
        running = false;
    }

    @Override
    public void start() {
        listener.start();
        running = true;
    }

    @Override
    public void stop() {
        listener.toStop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }


    static class ListenerThread extends Thread {

        private final int port;

        public ListenerThread(int port) {
            this.port = port;
            setName("ListenerTask-" + this.hashCode());
            setDaemon(true);
        }

        @Override
        public void run() {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();;
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline cp = ch.pipeline();
                                cp.addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS));
                                cp.addLast(new LengthFieldBasedFrameDecoder(8 * 1024 * 1024,
                                        1,
                                        4,
                                        -5,
                                        0));
                                cp.addLast(new MsgDecoder());
                                cp.addLast(new MsgEncoder());
                                cp.addLast(new TaskHandler());
                            }
                        });
                serverBootstrap.bind(port).sync();
                LockSupport.park();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        public void toStop() {
            LockSupport.unpark(this);
        }
    }


    static class TaskHandler extends SimpleChannelInboundHandler<TaskMsg> {

        private ThreadPoolExecutor pool = new ThreadPoolExecutor(
                12,
                24,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                });

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
            }, pool).thenAccept(res -> {
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

}

package org.github.taskstarter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.github.common.MsgDecoder;
import org.github.common.MsgEncoder;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.TimeUnit;
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

}

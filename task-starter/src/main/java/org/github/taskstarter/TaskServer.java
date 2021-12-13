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

import java.util.concurrent.TimeUnit;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class TaskServer {

    private final int port;

    public TaskServer(int port) {
        this.port = port;
    }

    public void start() {
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

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}

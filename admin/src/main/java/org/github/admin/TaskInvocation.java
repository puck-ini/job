package org.github.admin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.github.admin.entity.Point;
import org.github.common.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zengchzh
 * @date 2021/12/11
 */
@Slf4j
public class TaskInvocation {

    private final EventLoopGroup workGroup = new NioEventLoopGroup(1);

    private final Promise<Channel> cp = ImmediateEventExecutor.INSTANCE.newPromise();;

    private Channel channel;

    private Point point;

    private Map<Point, TaskInvocation> invocationMap;

    public TaskInvocation(Point point, Map<Point, TaskInvocation> invocationMap) {
        this.point = point;
        this.invocationMap = invocationMap;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline cp = ch.pipeline();
                            cp.addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
                            cp.addLast(new MsgEncoder());
                            cp.addLast(new LengthFieldBasedFrameDecoder(8 * 1024 * 1024,
                                    1,
                                    4,
                                    -5,
                                    0));
                            cp.addLast(new MsgDecoder());
                            cp.addLast(new InvocationHandler());
                        }
                    });
            bootstrap.connect(point.getIp(), point.getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        cp.trySuccess(future.channel());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        workGroup.shutdownGracefully();
    }


    public void invoke(TaskReq req) {
        if (Objects.isNull(channel)) {
            try {
                channel = cp.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        channel.writeAndFlush(TaskMsg.builder().msgType(MsgType.REQ).data(req).build());
    }

    class InvocationHandler extends SimpleChannelInboundHandler<TaskMsg> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TaskMsg msg) throws Exception {
            log.info(msg.toString());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                ctx.channel().writeAndFlush(TaskMsg.builder().msgType(MsgType.BEAT).build());
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("InvocationHandler exceptionCaught", cause);
            TaskInvocation.this.invocationMap.remove(TaskInvocation.this.point).disconnect();
            ctx.close();
        }
    }
}

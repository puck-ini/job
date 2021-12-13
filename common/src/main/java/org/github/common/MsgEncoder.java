package org.github.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class MsgEncoder extends MessageToByteEncoder<TaskMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TaskMsg msg, ByteBuf out) throws Exception {
        out.writeByte(msg.getMsgType().getCode());
        byte[] data = new JdkSerializer().serialize(msg.getData());
        out.writeInt(data.length + 5);
        out.writeBytes(data);
    }
}

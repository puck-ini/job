package org.github.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class MsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte msgCode = in.readByte();
        MsgType msgType = MsgType.get(msgCode);
        int dataLen = in.readInt();
        byte[] data = new byte[dataLen - 5];
        in.readBytes(data);
        TaskMsg taskMsg = TaskMsg.builder().msgType(msgType).build();
        JdkSerializer serializer = new JdkSerializer();
        if (msgType != null) {
            switch (msgType) {
                case RES:
                    taskMsg.setData(serializer.deserialize(data, TaskRes.class));
                    break;
                case REQ:
                    taskMsg.setData(serializer.deserialize(data, TaskReq.class));
                    break;
                case BEAT:
                    return;
                default:
                    break;
            }
        }
        out.add(taskMsg);
    }
}

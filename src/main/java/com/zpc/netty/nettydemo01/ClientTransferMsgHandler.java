package com.zpc.netty.nettydemo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientTransferMsgHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] buff = new byte[2024];
        int length = in.readableBytes();
        in.readBytes(buff, 0, length);

        String totalMsg = new String(buff, 0, length, StandardCharsets.UTF_8);
        String[] content = totalMsg.split("~");
        out.add(new Message(content[0], Utils.parseDateTime(content[1]), content[2]));
    }
}

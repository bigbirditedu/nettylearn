package com.bigbird.netty.httpdemo2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class MainAppClient {
    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        NettyHttpClient nettyHttpClient = new NettyHttpClient();
        nettyHttpClient.setHost("127.0.0.1");
        nettyHttpClient.setPort(8800);
        nettyHttpClient.init();
        ChannelFuture f = nettyHttpClient.connect();
        if (f != null) {
            URI uri = new URI("http://127.0.0.1:8800");
            String content = "hello netty http Server!";
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.GET, uri.toASCIIString(), Unpooled.wrappedBuffer(content.getBytes(StandardCharsets.UTF_8)));
            request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
            f.channel().write(request);//发送消息
            f.channel().flush();
        }

        Thread.sleep(1200000);
        nettyHttpClient.close();
    }
}

package com.bigbird.netty.httpdemo1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

public class HttpServer1 {
    public void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new HttpRequestDecoder()) // HTTP解码
                                .addLast("aggregator", new HttpObjectAggregator(2 * 1024 * 1024))//HTTP消息聚合
                                .addLast("encoder", new HttpResponseEncoder())// HTTP编码
                                .addLast("compressor", new HttpContentCompressor())//HttpContent压缩
                                .addLast("handler", new HttpServerHandler1());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        b.bind().sync();//同步阻塞，避免程序退出
        System.out.println("netty http server started on  port(" + port + ")");
    }

    public static void main(String[] args) throws InterruptedException {
        new HttpServer1().start(8800);
    }
}

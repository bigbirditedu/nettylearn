package com.bigbird.netty.httpdemo2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class NettyHttpClient {
    String host = "*";
    int port = 8800;

    int maxResponseContentLength = 1024 * 1024;

    EventLoopGroup workerGroup;
    Bootstrap bootstrap;

    public void init() {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("codec", new HttpClientCodec());
                        pipeline.addLast("decompressor", new HttpContentDecompressor());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(maxResponseContentLength));
                        pipeline.addLast("handler", new NettyHttpClientHandler());
                    }
                });

        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).sync();
            System.out.println("netty http client connected on host(" + host + ") port(" + port + ")");
        } catch (Exception e) {
            System.out.println("netty http client connect exception, host=" + host + ",port=" + port);
            System.exit(-1);
        }
        return channelFuture;
    }

    public void close() {
        System.out.println("stopping netty client");
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }

        System.out.println("netty client stopped");
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

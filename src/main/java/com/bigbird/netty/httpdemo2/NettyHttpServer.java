package com.bigbird.netty.httpdemo2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyHttpServer {
    String host = "*";
    int port = 8800;

    int backlog = 128;
    int maxContentLength = 1024 * 1024;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ServerBootstrap serverBootstrap;

    AtomicBoolean stopFlag = new AtomicBoolean();

    public NettyHttpServer() {
    }

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public void init() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("codec", new HttpServerCodec());// HTTP编解码
                        pipeline.addLast("decompressor", new HttpContentDecompressor());//HttpContent解压缩
                        pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));//HTTP 消息聚合
                        pipeline.addLast("compressor", new HttpContentCompressor());//HttpContent压缩
                        pipeline.addLast("handler", new NettyHttpServerHandler());//自定义业务逻辑处理器
                    }
                });
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, backlog);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    public void start() {
        InetSocketAddress addr = null;
        if (host == null || "*".equals(host)) {
            addr = new InetSocketAddress(port);
        } else {
            addr = new InetSocketAddress(host, port);
        }
        try {
            serverBootstrap.bind(addr).sync();
            System.out.println("netty http server started on host(" + addr.getHostName() + ") port(" + port + ")");
        } catch (Exception e) {
            System.out.println("netty http server bind exception, port=" + port);
            System.exit(-1);
        }
    }

    public void close() {
        System.out.println("stopping netty server");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        System.out.println("netty server stopped");
    }

    public void stop() {
        stopFlag.set(true);
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

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

}

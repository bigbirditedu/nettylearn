package com.zpc.netty.nettydemo01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatroomServerApp {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new MessageEncoder(), new ServerTransferMsgHandler(), new ServerMsgHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024 * 10)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

        File file = new File("index.jsp");
        RandomAccessFile rdf = new RandomAccessFile(file, "rw");

        byte[] arr = new byte[(int) file.length()];
        rdf.read(arr);

        Socket socket = new ServerSocket(8080).accept();
        socket.getOutputStream().write(arr);
    }
}

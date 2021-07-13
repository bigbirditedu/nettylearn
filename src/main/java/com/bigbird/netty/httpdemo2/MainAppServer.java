package com.bigbird.netty.httpdemo2;

public class MainAppServer {
    public static void main(String[] args) throws InterruptedException {
        NettyHttpServer nettyHttpServer = new NettyHttpServer();
        nettyHttpServer.init();
        nettyHttpServer.start();
        Thread.sleep(1200000);
        nettyHttpServer.close();
    }
}

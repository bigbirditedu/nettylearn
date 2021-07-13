package com.bigbird.netty.httpdemo4;

public class MyNettyHttpServerTest {
    public static void main(String[] args) throws InterruptedException {
        MyNettyHttpServer nettyHttpServer = new MyNettyHttpServer(8899);
        nettyHttpServer.init();
        nettyHttpServer.start();
        Thread.sleep(5 * 60 * 1000);
        nettyHttpServer.stop();
        nettyHttpServer.close();
    }
}

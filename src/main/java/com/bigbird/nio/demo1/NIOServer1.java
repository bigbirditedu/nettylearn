package com.bigbird.nio.demo1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOServer1 {
    private int port = 6666;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executorService;

    public NIOServer1() throws IOException {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        serverSocketChannel = ServerSocketChannel.open();
        //允许地址重用，即关闭了服务端程序之后，哪怕立即再启动该程序时可以顺利绑定相同的端口
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("server started...");
    }

    public static void main(String[] args) throws IOException {
        new NIOServer1().service();
    }

    private void service() {
        while (true) {
            SocketChannel socketChannel;
            try {
                socketChannel = serverSocketChannel.accept();
                executorService.execute(new NioHandler1(socketChannel));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class NioHandler1 implements Runnable {
    private SocketChannel socketChannel;

    public NioHandler1(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        Socket socket = socketChannel.socket();
        System.out.println("接受到客户端的连接，来自" + socket.getRemoteSocketAddress());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            String msg;
            while ((msg = reader.readLine()) != null) {
                System.out.println("客户端【" + socket.getInetAddress() + ":" + socket.getPort() + "】说：" + msg);
                writer.println(genResponse(msg));
                if ("bye".equals(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String genResponse(String msg) {
        return "服务器收到了您的消息：" + msg;
    }
}

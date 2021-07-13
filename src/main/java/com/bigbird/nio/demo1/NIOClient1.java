package com.bigbird.nio.demo1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class NIOClient1 {
    private SocketChannel socketChannel;

    public NIOClient1() throws IOException {
        socketChannel = SocketChannel.open();
        InetAddress localHost = InetAddress.getLocalHost();
        InetSocketAddress socketAddress = new InetSocketAddress(localHost, 6666);
        //采用阻塞模式连接服务器
        socketChannel.connect(socketAddress);
        System.out.println("与服务端连接成功！");
    }

    public static void main(String[] args) throws IOException {
        new NIOClient1().chat();
    }

    public void chat() {
        Socket socket = socketChannel.socket();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            while ((msg = inputReader.readLine()) != null) {
                writer.println(msg);
                System.out.println("【服务器】说:" + reader.readLine());
                //如果输入bye，则终止聊天
                if ("bye".equals(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

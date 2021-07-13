package com.bigbird.io.demo4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server4 {
    public static void main(String[] args) throws IOException {
        //开启一个TCP服务端,占用一个本地端口
        ServerSocket serverSocket = new ServerSocket(6662);
        //服务端循环不断地接受客户端的连接
        System.out.println("server start...");
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                System.out.println("客户端" + socket.getRemoteSocketAddress() + "上线了");
                //为每一个客户端分配一个线程
                Thread workThread = new Thread(new Handler(socket));
                workThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Handler implements Runnable {
    private Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //从Socket中获得输入输出流,接收和发送数据
            PrintWriter socketPrintWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = socketBufferedReader.readLine()) != null) {
                System.out.println("来自客户端" + socket.getRemoteSocketAddress() + "的消息：" + msg);
                String serverResponseMsg = "服务端收到了来自您的消息【" + msg + "】,并且探测到您的IP是：" + socket.getRemoteSocketAddress();
                socketPrintWriter.println(serverResponseMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //当与一个客户端通信结束后，需要关闭对应的socket
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.bigbird.io.demo1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server1 {
    public static void main(String[] args) throws IOException {
        //开启一个TCP服务端,占用一个本地端口
        ServerSocket serverSocket = new ServerSocket(6666);
        //服务端循环不断地接受客户端的连接
        while (true) {
            Socket socket = null;
            try {
                //与单个客户端通信的代码放在一个try代码块中，单个客户端发生异常(断开)时不影响服务端正常工作
                System.out.println("server start...");
                //下面这行代码会阻塞,直到有客户端连接
                socket = serverSocket.accept();
                System.out.println("客户端" + socket.getRemoteSocketAddress() + "上线了");
                //从Socket中获得输入输出流,接收和发送数据
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    String msg = new String(buf, 0, len);
                    System.out.println("来自客户端的消息：" + msg);
                    String serverResponseMsg = "服务端收到了来自您的消息【" + msg + "】,并且探测到您的IP是：" + socket.getRemoteSocketAddress();
                    outputStream.write(serverResponseMsg.getBytes(StandardCharsets.UTF_8));
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
}

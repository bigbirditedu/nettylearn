package com.bigbird.io.demo1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class Client1 {
    public static void main(String[] args) {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6666);
        try {
            socket.connect(address, 2000);
            OutputStream outputStream = socket.getOutputStream();
            String clientMsg = "服务端你好！我是客户端！你的IP是：" + socket.getRemoteSocketAddress();
            outputStream.write(clientMsg.getBytes(StandardCharsets.UTF_8));

            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                String msgFromServer = new String(buf, 0, len);
                System.out.println("来自服务端的消息:" + msgFromServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

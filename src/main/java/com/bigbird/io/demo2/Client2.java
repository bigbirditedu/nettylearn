package com.bigbird.io.demo2;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class Client2 {
    public static void main(String[] args) {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6666);
        try {
            socket.connect(address, 2000);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String clientMsg;
            System.out.println("请输入消息:");
            while ((clientMsg = bufferedReader.readLine()) != null) {
                outputStream.write(clientMsg.getBytes(StandardCharsets.UTF_8));
                byte[] buf = new byte[1024];
                int readLen = inputStream.read(buf);
                String msgFromServer = new String(buf, 0, readLen);
                System.out.println("来自服务端的消息:" + msgFromServer);
                System.out.println("请输入消息:");
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

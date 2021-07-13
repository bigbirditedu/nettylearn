package com.bigbird.io.demo4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client4 {
    public static void main(String[] args) {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6662);
        try {
            socket.connect(address, 2000);
            PrintWriter socketPrintWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader bufferedInputReader = new BufferedReader(new InputStreamReader(System.in));
            String clientMsg;
            System.out.println("请输入消息:");
            while ((clientMsg = bufferedInputReader.readLine()) != null) {
                socketPrintWriter.println(clientMsg);
                String msgFromServer = socketBufferedReader.readLine();
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

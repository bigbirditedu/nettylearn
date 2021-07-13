package com.bigbird.nio.demo3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NIOClient3 {
    private ByteBuffer recvBuf = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuf = ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel socketChannel;
    private Selector selector;

    public NIOClient3() throws IOException {
        socketChannel = SocketChannel.open();
        InetAddress localHost = InetAddress.getLocalHost();
        InetSocketAddress socketAddress = new InetSocketAddress(localHost, 6666);
        //采用阻塞模式连接服务器
        socketChannel.connect(socketAddress);
        //设置为非阻塞模式
        socketChannel.configureBlocking(false);
        System.out.println("与服务端连接成功！");
        selector = Selector.open();
    }

    public static void main(String[] args) throws IOException {
        NIOClient3 nioClient3 = new NIOClient3();
        Thread inputThread = new Thread() {
            @Override
            public void run() {
                nioClient3.sendInputMsg();
            }
        };

        inputThread.start();
        nioClient3.receiveMsg();
    }

    private void receiveMsg() throws IOException {
        socketChannel.register(selector, SelectionKey.OP_READ);
        while (selector.select() > 0) {
            for (SelectionKey key : selector.selectedKeys()) {
                try {
                    selector.selectedKeys().remove(key);
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        String msg = "";
                        while (sc.read(buffer) > 0) {
                            buffer.flip();
                            msg += charset.decode(buffer);
                        }
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        //发生异常时，使这个SelectionKey失效，Selector不再监控这个SelectionKey感兴趣的事件
                        if (key != null) {
                            key.cancel();
                            //关闭这个SelectionKey关联的SocketChannel
                            key.channel().close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendInputMsg() {
        //接收键盘输入的消息并发送数据到服务器
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            while ((msg = inputReader.readLine()) != null) {
                socketChannel.write(charset.encode(msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

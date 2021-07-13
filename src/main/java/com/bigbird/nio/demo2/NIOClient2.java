package com.bigbird.nio.demo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOClient2 {
    private ByteBuffer recvBuf = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuf = ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel socketChannel;
    private Selector selector;

    public NIOClient2() throws IOException {
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
        NIOClient2 nioClient2 = new NIOClient2();
        Thread inputThread = new Thread() {
            @Override
            public void run() {
                nioClient2.receiveInput();
            }
        };

        inputThread.start();
        nioClient2.chat();
    }

    private void chat() throws IOException {
        //接收和发送数据
        socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        while (selector.select() > 0) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = null;
                try {
                    key = iterator.next();
                    iterator.remove();
                    if (key.isWritable()) {
                        sendMsg(key);
                    }

                    if (key.isReadable()) {
                        receiveMsg(key);
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

    private void receiveMsg(SelectionKey key) throws IOException {
        //接收服务端发来的数据，放到recvBuf中，如满一行数据，就输出，然后从recvBuf中删除
        SocketChannel channel = (SocketChannel) key.channel();
        channel.read(recvBuf);
        recvBuf.flip();
        String recvMsg = decode(recvBuf);
        if (recvMsg.indexOf("\n") == -1) {
            return;
        }
        String recvMsgLine = recvMsg.substring(0, recvMsg.indexOf("\n") + 1);
        System.out.print("【服务器】说:" + recvMsgLine);
        if (recvMsgLine.contains("bye")) {
            key.cancel();
            socketChannel.close();
            System.out.println("与服务器断开连接");
            selector.close();
            System.exit(0);
        }

        ByteBuffer temp = encode(recvMsgLine);
        recvBuf.position(temp.limit());
        //删除已经输出的数据
        recvBuf.compact();
    }

    private void sendMsg(SelectionKey key) throws IOException {
        //发送sendBuf中的数据
        SocketChannel channel = (SocketChannel) key.channel();
        synchronized (sendBuf) {
            //为取出数据做好准备
            sendBuf.flip();
            //将sendBuf中的数据写入到Channel中去
            channel.write(sendBuf);
            //删除已经发送的数据(通过压缩的方式)
            sendBuf.compact();
        }
    }

    private void receiveInput() {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            while ((msg = inputReader.readLine()) != null) {
                synchronized (sendBuf) {
                    sendBuf.put(encode(msg + "\r\n"));
                }
                //如果输入bye，则终止聊天
                if ("bye".equals(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer encode(String msg) {
        return charset.encode(msg);//转为字节
    }

    private String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);//转为字符
        return charBuffer.toString();
    }
}

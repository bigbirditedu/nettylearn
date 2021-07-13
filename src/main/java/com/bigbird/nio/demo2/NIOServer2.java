package com.bigbird.nio.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NIOServer2 {
    private int port = 6666;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");

    public NIOServer2() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        //设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("server started...");
    }

    public static void main(String[] args) throws IOException {
        new NIOServer2().service();
    }

    private void service() throws IOException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = null;
                //处理每个SelectionKey的代码放在一个try/catch块中，如果出现异常，就使其失效并关闭对应的Channel
                try {
                    key = iterator.next();
                    if (key.isAcceptable()) {
                        doAccept(key);
                    }
                    if (key.isWritable()) {
                        sendMsg(key);
                    }

                    if (key.isReadable()) {
                        receiveMsg(key);
                    }
                    //从Selector的selected-keys集合中删除处理过的SelectionKey
                    iterator.remove();
                } catch (Exception e) {
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
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //创建一个ByteBuffer存放读取到的数据
        ByteBuffer readBuffer = ByteBuffer.allocate(64);
        socketChannel.read(readBuffer);
        readBuffer.flip();
        buffer.limit(buffer.capacity());
        //把readBuffer中的数据拷贝到buffer中，假设buffer的容量足够大，不会出现溢出的情况
        //在非阻塞模式下，socketChannel.read(readBuffer)方法一次读入多少字节的数据是不确定的，无法保证一次读入的是一整行字符串数据
        //因此需要将其每次读取的数据放到buffer中，当凑到一行数据时再回复客户端
        buffer.put(readBuffer);
    }

    private void sendMsg(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.flip();
        String data = decode(buffer);
        //当凑满一行数据时再回复客户端
        if (data.indexOf("\r\n") == -1) {
            return;
        }
        //读取一行数据
        String recvData = data.substring(0, data.indexOf("\n") + 1);
        System.out.print("客户端【" + socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort() + "】说：" + recvData);
        ByteBuffer outputBuffer = encode(genResponse(recvData));
        while (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer);
        }

        ByteBuffer temp = encode(recvData);
        buffer.position(temp.limit());
        //删除buffer中已经处理过的数据
        buffer.compact();

        if ("bye\r\n".equals(recvData)) {
            key.cancel();
            key.channel().close();
            System.out.println("关闭与客户端" + socketChannel.socket().getRemoteSocketAddress() + "的连接");
        }
    }

    private ByteBuffer encode(String msg) {
        return charset.encode(msg);//转为字节
    }

    private String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);//转为字符
        return charBuffer.toString();
    }

    private void doAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = ssc.accept();
        System.out.println("接受到客户端的连接，来自" + socketChannel.socket().getRemoteSocketAddress());
        //设置为非阻塞模式
        socketChannel.configureBlocking(false);
        //创建一个用于接收客户端数据的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //向Selector注册读、写就绪事件,并关联一个buffer附件
        socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, buffer);
    }

    private String genResponse(String msg) {
        return "服务器收到了您的消息：" + msg;
    }
}

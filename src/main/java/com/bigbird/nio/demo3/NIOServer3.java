package com.bigbird.nio.demo3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;

public class NIOServer3 {
    private int port = 6666;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");

    public NIOServer3() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        //设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("server started...");
    }

    public static void main(String[] args) throws IOException {
        new NIOServer3().service();
    }

    private void service() throws IOException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            for (SelectionKey key : selector.selectedKeys()) {
                selector.selectedKeys().remove(key);
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = ssc.accept();
                    System.out.println("接受到客户端的连接，来自" + socketChannel.socket().getRemoteSocketAddress());
                    //设置为非阻塞模式
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    String msg = "";
                    try {
                        while (sc.read(buffer) > 0) {
                            buffer.flip();
                            msg += charset.decode(buffer);
                        }
                        System.out.println("客户端【" + sc.getRemoteAddress() + "】说：" + msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            //对某个Client对应的Channel读写发生异常时，使这个SelectionKey失效，Selector不再监控这个SelectionKey感兴趣的事件
                            if (key != null) {
                                key.cancel();
                                //关闭这个SelectionKey关联的SocketChannel
                                System.out.println("客户端【" + ((SocketChannel) key.channel()).socket().getRemoteSocketAddress() + "】下线了");
                                key.channel().close();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (msg.length() > 0) {
                        for (SelectionKey selectedKey : selector.keys()) {
                            Channel channel = selectedKey.channel();
                            //遍历Selector中的所有注册的Channel，如果是客户端的SocketChannel，则群发消息，并排除自己
                            if (channel instanceof SocketChannel && channel != sc) {
                                SocketChannel socketChannel = (SocketChannel) channel;
                                socketChannel.write(charset.encode("用户【" + sc.getRemoteAddress() + "】说：" + msg));
                            }
                        }
                    }
                }
            }
        }
    }
}

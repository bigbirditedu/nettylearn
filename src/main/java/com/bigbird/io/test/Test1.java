package com.bigbird.io.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Test1 {
    public static void main(String[] args) {
//        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
//        buffer.writeBytes("你好啊".getBytes(StandardCharsets.UTF_8));
//        if (buffer.hasArray()) {
//            byte[] array = buffer.array();
//            int i = buffer.arrayOffset();
//            int j = buffer.readerIndex();
//            System.out.println(i);
//            System.out.println(j);
//            int offset = i + j;
//            int len = buffer.readableBytes();
//            System.out.println(offset);
//            System.out.println(len);
//            String string = new String(array, offset, len, Charset.forName("UTF-8"));
//            System.out.println(string);
//        }

        ByteBuf buffer = ByteBufAllocator.DEFAULT.directBuffer();
        buffer.writeBytes("你好啊2".getBytes(StandardCharsets.UTF_8));
        if (!buffer.hasArray()) {
            int len = buffer.readableBytes();
            byte[] array = new byte[len];
            buffer.getBytes(buffer.readerIndex(), array);
            String string = new String(array, Charset.forName("UTF-8"));
            System.out.println(string);
        }

        buffer.release();
    }
}

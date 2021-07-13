package com.bigbird.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileCopy07 {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("D:\\file01.txt");
        FileOutputStream fos = new FileOutputStream("D:\\file01_copy07.txt");
        FileChannel srcChannel = fis.getChannel();
        FileChannel destChannel = fos.getChannel();
        long size = srcChannel.size();
        long position = 0;
        while (size > 0) {
            long count = srcChannel.transferTo(position, srcChannel.size(), destChannel);
            position += count;
            size -= count;
        }

        destChannel.close();
        srcChannel.close();
        fis.close();
        fos.close();
    }
}

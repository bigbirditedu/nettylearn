package com.bigbird.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileCopy06 {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("D:\\file01.txt");
        FileOutputStream fos = new FileOutputStream("D:\\file01_copy06.txt");
        FileChannel srcChannel = fis.getChannel();
        FileChannel destChannel = fos.getChannel();
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        destChannel.close();
        srcChannel.close();
        fis.close();
        fos.close();
    }
}

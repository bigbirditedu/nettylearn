package com.bigbird.io;

import java.io.RandomAccessFile;

public class FileCopy03 {
    public static void main(String[] args) {
        //使用jdk7引入的自动关闭资源的try语句
        try (RandomAccessFile in = new RandomAccessFile("D:\\file01.txt", "rw");
             RandomAccessFile out = new RandomAccessFile("D:\\file01_copy3.txt", "rw")) {
            byte[] buf = new byte[2];
            int hasRead = 0;
            while ((hasRead = in.read(buf)) > 0) {
                //每次读取多少就写多少
                out.write(buf, 0, hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.bigbird.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileCopy01 {
    public static void main(String[] args) {
        //使用jdk7引入的自动关闭资源的try语句
        try (FileInputStream fis = new FileInputStream("D:\\file01.txt");
             FileOutputStream fos = new FileOutputStream("D:\\file01_copy.txt")) {
            byte[] buf = new byte[126];
            int hasRead = 0;
            while ((hasRead = fis.read(buf)) > 0) {
                //每次读取多少就写多少
                fos.write(buf, 0, hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

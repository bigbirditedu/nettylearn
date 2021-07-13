package com.bigbird.io;

import java.io.FileReader;
import java.io.FileWriter;

public class FileCopy02 {
    public static void main(String[] args) {
        //使用jdk7引入的自动关闭资源的try语句
        try (FileReader fr = new FileReader("D:\\file01.txt");
             FileWriter fw = new FileWriter("D:\\file01_copy2.txt")) {
            char[] buf = new char[2];
            int hasRead = 0;
            while ((hasRead = fr.read(buf)) > 0) {
                //每次读取多少就写多少
                fw.write(buf, 0, hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

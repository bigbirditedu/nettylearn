package com.bigbird.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopy05 {
    public static void main(String[] args) {
        File f = new File("D:\\file01.txt");
        try (FileInputStream fis = new FileInputStream(f);
             FileOutputStream fos = new FileOutputStream("D:\\file01_copy05.txt");
             FileChannel inc = fis.getChannel();
             FileChannel outc = fos.getChannel()
        ) {
            //将FileChannel里的全部数据映射到ByteBuffer中
            MappedByteBuffer mappedByteBuffer = inc.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
            outc.write(mappedByteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package br.com.wasys.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by pascke on 03/07/17.
 */

public class FileUtils {

    public static void copy(File origin, File destination) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(origin);
        FileOutputStream fileOutputStream = new FileOutputStream(destination);
        FileChannel inFileChannel = fileInputStream.getChannel();
        FileChannel outFileChannel = fileOutputStream.getChannel();
        inFileChannel.transferTo(0, inFileChannel.size(), outFileChannel);
        fileInputStream.close();
        fileOutputStream.close();
    }
}

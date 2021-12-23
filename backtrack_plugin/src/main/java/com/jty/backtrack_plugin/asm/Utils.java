package com.jty.backtrack_plugin.asm;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author jty
 * @date 2021/10/26
 */
public class Utils {

    /**
     * 广度优先遍历
     */
    public static List<File> eachFile(File file) {
        List<File> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        Queue<File> foreachQueue = new LinkedList<>();
        foreachQueue.add(file);
        while (foreachQueue.size() > 0) {
            File poll = foreachQueue.poll();
            if (poll != null) {
                if (poll.isDirectory()) {
                    foreachQueue.addAll(Arrays.asList(poll.listFiles()));
                } else {
                    result.add(poll);
                }
            }
        }
        return result;
    }

    public static final int BUFFER_SIZE = 16384;

    public static String readFileAsString(File file) {
        if (!file.exists()) {
            return "";
        }
        StringBuffer fileData = new StringBuffer();
        Reader fileReader = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
            fileReader = new InputStreamReader(inputStream, "UTF-8");
            char[] buf = new char[BUFFER_SIZE];
            int numRead = 0;
            while ((numRead = fileReader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
        } catch (Exception e) {
            System.out.printf("file op readFileAsString e type:%s, e msg:%s, filePath:%s%n",
                    e.getClass().getSimpleName(), e.getMessage(), file.getAbsolutePath());
            return "";
        } finally {
            try {
                closeQuietly(fileReader);
                closeQuietly(inputStream);
            } catch (Exception e) {
                System.out.printf( "file op readFileAsString close e type:%s, e msg:%s, filePath:%s",
                        e.getClass().getSimpleName(), e.getMessage(), file.getAbsolutePath());
            }
        }
        return fileData.toString();
    }

    /**
     * Closes the given {@code Closeable}. Suppresses any IO exceptions.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {

        }
    }
}

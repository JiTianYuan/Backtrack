package com.jty.backtrack_plugin.asm;

import java.io.File;
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
}

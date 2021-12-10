package com.jty.backtrack.data_parser.worker;

import com.jty.backtrack.data_parser.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

/**
 * @author jty
 * @date 2021/11/30
 * <p>
 * 读取mapping文件
 */
class MappingReader {
    private static final boolean DEBUG = true;

    private HashMap<Integer, String> methodMapping = new HashMap<>();

    //加载mapping文件
    public HashMap<Integer, String> loadMapping(String mappingPath) {
        File mappingFilePath = new File(mappingPath);
        List<File> files = Util.eachFile(mappingFilePath);
        for (File file : files) {
            if (file.getName().endsWith(".txt")) {
                if (DEBUG) {
                    System.out.println("[处理mapping文件]:" + file.getName());
                }
                readFile(file);
            }
        }
        return methodMapping;
    }

    private void readFile(File file) {
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader r = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(r);

            String lineTxt = null;

            lineTxt = reader.readLine();
            if ("#Backtrack Mapping".equals(lineTxt)) {
                //通过第一行确认是否是Backtrack的的mapping
                while ((lineTxt = reader.readLine()) != null) {
                    parseLine(lineTxt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseLine(String line) {
        try {
            String[] split = line.split("-->");
            if (split.length == 2) {
                int id = Integer.valueOf(split[0]);
                String methodName = split[1];
                if (methodMapping.containsKey(id)) {
                    if (DEBUG) {
                        System.out.println("[MappingMap 重复]:" + line);
                    }
                } else {
                    methodMapping.put(id, methodName);
                    if (DEBUG) {
                        System.out.println("[MappingMap put]:" + line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

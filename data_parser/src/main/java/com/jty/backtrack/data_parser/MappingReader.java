package com.jty.backtrack.data_parser;

import java.io.File;
import java.util.HashMap;

/**
 * @author jty
 * @date 2021/11/30
 * <p>
 * 读取mapping文件
 */
class MappingReader {

    private HashMap<Integer, String> methodMapping = new HashMap<>();

    //加载mapping文件
    public void loadMapping(String mappingPath) {
        File mappingFilePath = new File(mappingPath);
        if (mappingFilePath.exists()) {
            if (mappingFilePath.isDirectory()) {
                //文件夹，遍历
            } else {
                //单独的文件
            }
        }
    }


    public String getMethodName(int methodId) {
        String methodName = methodMapping.get(methodId);
        return methodName != null ? methodName : String.valueOf(methodId);
    }

}

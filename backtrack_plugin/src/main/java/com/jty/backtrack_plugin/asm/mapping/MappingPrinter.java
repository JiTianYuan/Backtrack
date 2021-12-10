package com.jty.backtrack_plugin.asm.mapping;

import com.android.ddmlib.Log;
import com.jty.backtrack_plugin.asm.MethodItem;
import com.jty.backtrack_plugin.asm.collector.MethodCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jty
 * @date 2021/10/30
 */
public class MappingPrinter {
    private static final String TAG = "MappingPrinter";

    private static final String MAPPING_NAME = "methodMapping.txt";
    private static final String IGNORE_MAPPING_NAME = "ignoreMethodMapping.txt";


    private final MethodCollector mMethodCollector;

    public MappingPrinter(MethodCollector methodCollector) {
        this.mMethodCollector = methodCollector;
    }

    public void print(String mappingOutDir) {
        File methodMapping = new File(mappingOutDir + File.separatorChar + MAPPING_NAME);
        File ignoreMethodMapping = new File(mappingOutDir + File.separatorChar + IGNORE_MAPPING_NAME);

        saveMapping(methodMapping);
        saveIgnoreMapping(ignoreMethodMapping);
    }

    private void saveMapping(File methodMappingFile) {
        if (!methodMappingFile.getParentFile().exists()) {
            methodMappingFile.getParentFile().mkdirs();
        }
        List<MethodItem> methodList = new ArrayList<>();
        methodList.addAll(mMethodCollector.getCollectedMethodMap().values());
        Log.i(TAG, "saveMapping count = " + methodList.size() + ",path:" + methodMappingFile.getAbsolutePath());
        Collections.sort(methodList, new Comparator<MethodItem>() {
            @Override
            public int compare(MethodItem o1, MethodItem o2) {
                return o1.id - o2.id;
            }
        });

        PrintWriter pw = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(methodMappingFile, false);
            Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
            pw = new PrintWriter(w);
            //mapping文件头
            pw.println("#Backtrack Mapping");
            for (MethodItem traceMethod : methodList) {
                pw.println(traceMethod.id + "-->" + traceMethod.getMethodName());
            }
        } catch (Exception e) {
            Log.e(TAG, "write method map Exception:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

    private void saveIgnoreMapping(File ignoreMappingFile) {
        if (!ignoreMappingFile.getParentFile().exists()) {
            ignoreMappingFile.getParentFile().mkdirs();
        }
        List<MethodItem> methodList = new ArrayList<>();
        methodList.addAll(mMethodCollector.getCollectedIgnoreMethodMap().values());
        Log.i(TAG, "saveIgnoreMapping count = " + methodList.size() + ",path:" + ignoreMappingFile.getAbsolutePath());

        PrintWriter pw = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(ignoreMappingFile, false);
            Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
            pw = new PrintWriter(w);
            for (MethodItem traceMethod : methodList) {
                pw.println(traceMethod.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "write method map Exception:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }
}

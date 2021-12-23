package com.jty.backtrack_plugin.asm;

import java.util.HashSet;

/**
 * @author jty
 * @date 2021/10/11
 */
public class ASMConfig {
    public static final String METHOD_TRACE_CLASS = "com/jty/backtrack/core/Backtrack";
    public static final String METHOD_TRACE_IN = "i";
    public static final String METHOD_TRACE_OUT = "o";
    public static final String METHOD_TRACE_CATCH = "t";

    public static final String mappingDir = "";


    private static final String[] UN_TRACE_CLASS = {"R.class", "R$", "Manifest", "BuildConfig"};

    public static final String[] DEFAULT_WHITE_PACKAGE = {
            "com/jty/backtrack/"
    };

//    public static boolean isNeedTraceClass(String clsName) {
//        boolean isNeed = true;
//        clsName = clsName.replaceAll("/", ".");
//        if (clsName.startsWith("com.jty.backtrack.")) {
//            isNeed = false;
//        }
//        return isNeed;
//    }


    /**
     * class白名单
     */
    public static boolean isNeedTraceFile(String fileName) {
        if (fileName.endsWith(".class")) {
            for (String unTraceCls : ASMConfig.UN_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}

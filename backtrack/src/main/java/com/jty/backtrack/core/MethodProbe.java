package com.jty.backtrack.core;

import android.util.Log;

/**
 * @author jty
 * @date 2021/10/11
 * <p>
 * 方法入口出口插桩
 */
public class MethodProbe {
    private static final String TAG = "MethodProbe";

    public static void i(String methodName) {
        Log.i(TAG, "method in = " + methodName);
    }

    public static void i(int i) {
        Log.i(TAG, "method in = " + i);
    }


    public static void o(String methodName) {
        Log.i(TAG, "method out = " + methodName);
    }
}

package com.jty.backtrack.core;

import android.util.Log;

/**
 * @author jty
 * @date 2022/1/16
 */
public class BacktrackRuntimeConfig {
    private static final String TAG = "BacktrackRuntimeConfig";
    private static boolean sHasBootEndTag = false;

    static {
        Log.d(TAG, "start to init runtime config");
    }

    public static boolean isHasBootEndTag() {
        return sHasBootEndTag;
    }

    public static void setHasBootEndTag(boolean hasBootEndTag) {
        sHasBootEndTag = hasBootEndTag;
    }
}

package com.jty.backtrack.frame_monitor;

import android.util.Log;

/**
 * @author jty
 * @date 2021/11/01
 */
final class FrameInfo {
    private static final String TAG = "FrameInfo";
    private static final boolean DEBUG = false;

    // 预期的vsync时间
    private static final int INTENDED_VSYNC = 0;

    // doFrame 的执行时间
    private static final int DO_FRAME_START = 1;

    // 输入事件开始处理的时间
    private static final int HANDLE_INPUT_START = 2;

    // 动画开始的时间
    private static final int HANDLE_ANIMATION_START = 3;

    // ViewRootImpl#performTraversals()
    private static final int PERFORM_TRAVERSALS_START = 4;

    private static final int DO_FRAME_END = 5;

    private long[] mFrameInfo = new long[6];


    void markDoFrameStart(long intendedVsync) {
        mFrameInfo[INTENDED_VSYNC] = intendedVsync;
        mFrameInfo[DO_FRAME_START] = System.nanoTime();
        if (DEBUG) {
            Log.i(TAG, "markDoFrameStart:" + mFrameInfo[DO_FRAME_START]);
        }
    }

    void markInputStart() {
        mFrameInfo[HANDLE_INPUT_START] = System.nanoTime();
    }

    void markAnimationStart() {
        mFrameInfo[HANDLE_ANIMATION_START] = System.nanoTime();
    }

    void markTraversalsStart() {
        mFrameInfo[PERFORM_TRAVERSALS_START] = System.nanoTime();
    }

    void markDoFrameEnd() {
        mFrameInfo[DO_FRAME_END] = System.nanoTime();
        if (DEBUG) {
            Log.i(TAG, "markDoFrameEnd:" + mFrameInfo[DO_FRAME_END]);
        }
    }

    void dump() {
        String log = "[FrameInfo]: " +
                "帧处理时间 = " + nano2Ms(mFrameInfo[DO_FRAME_END] - mFrameInfo[INTENDED_VSYNC]) +
                "，handler调度 = " + nano2Ms(mFrameInfo[DO_FRAME_START] - mFrameInfo[INTENDED_VSYNC]) +
                "，input = " + nano2Ms(mFrameInfo[HANDLE_ANIMATION_START] - mFrameInfo[HANDLE_INPUT_START]) +
                "，animation = " + nano2Ms(mFrameInfo[PERFORM_TRAVERSALS_START] - mFrameInfo[HANDLE_ANIMATION_START]) +
                "，traversals = " + nano2Ms(mFrameInfo[DO_FRAME_END] - mFrameInfo[PERFORM_TRAVERSALS_START]);

        Log.i(TAG, log);
    }

    long getFrameDurationNanos() {
        //结束时间 - 预期的起始帧时间 这样可以包含handler调度的时间，可以发现handler调度造成的卡顿
        return mFrameInfo[DO_FRAME_END] - mFrameInfo[INTENDED_VSYNC];
    }

    private static float nano2Ms(long nanoTime) {
        return nanoTime * 0.000001f;
    }

}

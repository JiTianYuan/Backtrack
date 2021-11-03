package com.jty.backtrack.core;

import android.os.Looper;
import android.util.Log;

import com.jty.backtrack.frame_monitor.FrameMonitor;

/**
 * @author jty
 * @date 2021/10/30
 * <p>
 * api接口
 */
public class Backtrack {
    private static final String TAG = "Backtrack";
    private static final boolean DEBUG = true;

    private static Backtrack mInstance;

    private final Config mConfig;
    private final BacktraceStack mBacktraceStack;

    public synchronized static void init(Config config) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("must be init in main thread!!!");
        }
        //初始化卡顿监控
        FrameMonitor.init();
        mInstance = new Backtrack(config);
    }

    public synchronized static Backtrack getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("must be init before use!!!");
        }
        return mInstance;
    }


    private Backtrack(Config config) {
        mConfig = config;
        //初始化回溯堆栈
        mBacktraceStack = new BacktraceStack();
        FrameMonitor.getInstance().addFrameObserver(mBacktraceStack);
    }

    /**
     * 插桩调用，方法入口
     *
     * @param id 方法id
     */
    public static void i(int id) {
        if (DEBUG) {
            Log.i(TAG, "method in = " + id);
        }
        if (mInstance == null || mInstance.mBacktraceStack == null) {
            return;
        }
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            //主线程判断，只有主线程插桩
            return;
        }
        mInstance.mBacktraceStack.record(id,true);
    }

    /**
     * 插桩调用，方法出口
     *
     * @param id 方法id
     */
    public static void o(int id) {
        if (DEBUG) {
            Log.i(TAG, "method out = " + id);
        }
        if (mInstance == null || mInstance.mBacktraceStack == null) {
            return;
        }
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            //主线程判断，只有主线程插桩
            return;
        }
        mInstance.mBacktraceStack.record(id,false);
    }


}

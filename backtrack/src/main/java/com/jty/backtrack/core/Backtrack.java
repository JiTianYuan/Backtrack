package com.jty.backtrack.core;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.jty.backtrack.frame_monitor.FrameMonitor;
import com.jty.backtrack.store.IOutputProcessor;
import com.jty.backtrack.store.OutputProcessorImpl;

import java.io.File;

/**
 * @author jty
 * @date 2021/10/30
 * <p>
 * api接口
 */
public class Backtrack implements BacktrackContext {
    public static final String TAG = "Backtrack";
    public static final boolean DEBUG = true;

    private static Backtrack mInstance;

    private final long mUIThreadId;
    private final int mProcessId;
    private final Config mConfig;
    private final BacktraceStack mBacktraceStack;
    private final IOutputProcessor mOutputProcessor;
    private final long mFrameIntervalNanos;//系统一帧的间隔
    private final String mPkgName;

    public synchronized static void init(Context context, Config config) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("must be init in main thread!!!");
        }
        //初始化卡顿监控
        FrameMonitor.init();
        mInstance = new Backtrack(context, config);
    }

    public synchronized static Backtrack getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("must be init before use!!!");
        }
        return mInstance;
    }


    private Backtrack(Context context, Config config) {
        mUIThreadId = Looper.getMainLooper().getThread().getId();
        mProcessId = Process.myPid();
        mPkgName = context.getPackageName();

        mConfig = config;
        if (TextUtils.isEmpty(mConfig.getOutputDir())) {
            String path = context.getFilesDir().getAbsolutePath() + File.separator + "Backtrack";
            mConfig.setOutputDir(path);
        }

        mFrameIntervalNanos = FrameMonitor.getInstance().getFrameIntervalNanos();
        //初始化回溯堆栈
        long frameTimeThreshold = mConfig.getJankFrameThreshold() * mFrameIntervalNanos;
        mBacktraceStack = new BacktraceStack(this, frameTimeThreshold);
        FrameMonitor.getInstance().addFrameObserver(mBacktraceStack);
        mOutputProcessor = new OutputProcessorImpl(this);
    }

    /**
     * 插桩调用，方法入口
     *
     * @param id 方法id
     */
    public static void i(int id) {
        if (mInstance == null || mInstance.mBacktraceStack == null) {
            return;
        }
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            //主线程判断，只有主线程插桩
            return;
        }
        if (DEBUG) {
            Log.i(TAG, "method in = " + id);
        }
        mInstance.mBacktraceStack.record(id, true);
    }

    /**
     * 插桩调用，方法出口
     *
     * @param id 方法id
     */
    public static void o(int id) {
        if (mInstance == null || mInstance.mBacktraceStack == null) {
            return;
        }
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            //主线程判断，只有主线程插桩
            return;
        }
        if (DEBUG) {
            Log.i(TAG, "method out = " + id);
        }
        mInstance.mBacktraceStack.record(id, false);
    }


    @Override
    public boolean isDebug() {
        return mConfig.isDebuggable();
    }

    @Override
    public Config getConfig() {
        return mConfig;
    }

    @Override
    public IOutputProcessor getOutputProcessor() {
        return mOutputProcessor;
    }

    @Override
    public long getUIThreadId() {
        return mUIThreadId;
    }

    @Override
    public int getProcessId() {
        return mProcessId;
    }

    @Override
    public String getPkgName() {
        return mPkgName;
    }
}

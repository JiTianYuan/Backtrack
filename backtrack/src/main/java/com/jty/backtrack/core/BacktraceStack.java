package com.jty.backtrack.core;

import android.util.Log;

import com.jty.backtrack.frame_monitor.FrameMonitor;
import com.jty.backtrack.utils.StatusSpec;

import java.util.Arrays;

import static com.jty.backtrack.core.Backtrack.TAG;

/**
 * @author jty
 * @date 2021/10/30
 * <p>
 * 回溯堆栈
 * 线程安全，只考虑主线程
 * <p>
 * 记录方法状态的数组：long类型，64位，前两位表示状态(入栈、出栈、异常)，后62位表示时间（精确到微秒）
 * 记录方法id的数组：int类型，保存方法id
 * 所以，一个方法入栈占用96位，12字节。
 */
class BacktraceStack implements FrameMonitor.FrameObserver {
    /**
     * 初始堆栈大小
     * 12字节 x 1024 x 1024 = 12M;
     */
    public static final int DEFAULT_STACK_SIZE = 1024 * 1024;
    /**
     * 扩容因子，扩容后的容量是当前容量的 x倍
     */
    private static final float EXPAND_FACTOR = 1.5f;

    private final BacktrackContext mContext;

    /**
     * 记录方法状态
     * 第一位1表示进栈，0表示出栈。剩下63位表示时间，微秒单位
     */
    private long[] mStatusStack;

    /**
     * 记录方法id
     */
    private int[] mIdStack;

    private int mStackSize;
    /**
     * 堆栈有效数据指针
     */
    private int mPoint = 0;

    /**
     * 时间阈值，一帧的时间大于阈值，就记录
     */
    private long mFrameTimeThreshold;

    private long mStartUpTime;

    //可能有脏数据：刚从BOOT_MODE 切换到 JANK_MODE 时，由于BOOT_MODE结束会导出堆栈，第一帧的的数据可能不全，需要丢弃
    private boolean mMaybeHaveDirtyData;

    /**
     * 当前的记录模式
     */
    private RecordMode mCurMode;

    protected BacktraceStack(BacktrackContext context, long frameTimeThreshold, int initialStackSize) {
        mContext = context;
        mFrameTimeThreshold = frameTimeThreshold;
        mStackSize = initialStackSize > 0 ? initialStackSize : DEFAULT_STACK_SIZE;
        mStatusStack = new long[mStackSize];
        mIdStack = new int[mStackSize];
        mCurMode = RecordMode.JANK_MODE;
    }

    protected void record(int methodId, long status) {
        checkExpand();
        long time = System.nanoTime() / 1000; //纳秒转微秒
        mStatusStack[mPoint] = StatusSpec.makeStatusSpec(status, time);
        mIdStack[mPoint] = methodId;
        mPoint++;
    }


    protected void setMode(RecordMode mode) {
        if (mCurMode == mode) {
            return;
        }
        if (mode == RecordMode.BOOT_MODE) {
            mStartUpTime = System.nanoTime();
        } else {
            if (mCurMode == RecordMode.BOOT_MODE) {
                //结束启动记录模式，dump堆栈
                record(0, StatusSpec.STATUS_FORCE_DUMP);
                dump(System.nanoTime() - mStartUpTime);
                mMaybeHaveDirtyData = true;
            }
        }
        mCurMode = mode;
        if (mContext.isDebug()) {
            Log.i(TAG, "setMode = " + mode.name());
        }
    }

    /**
     * 丢弃当前堆栈
     */
    private void discard() {
        mPoint = 0;
    }

    /**
     * 保存当前堆栈
     */
    private void dump(long frameDurationNanos) {
        if (mPoint == 0) {
            return;
        }
        long start = System.currentTimeMillis();

        long[] dumpStatusStack = Arrays.copyOf(mStatusStack, mPoint);
        int[] dumpIdStack = Arrays.copyOf(mIdStack, mPoint);
        //保存到文件
        mContext.getOutputProcessor().saveBacktraceStack(mCurMode, frameDurationNanos, dumpStatusStack, dumpIdStack);
        if (mContext.isDebug()) {
            Log.i(TAG, "Dump，堆栈容量 = " + mPoint + ",耗时 = " + (System.currentTimeMillis() - start));
        }
        discard();
    }

    /**
     * 检测是否需要扩容
     */
    private void checkExpand() {
        if (mPoint >= mStackSize - 1) {
            //扩容
            long start = System.currentTimeMillis();
            mStackSize = (int) (mStackSize * EXPAND_FACTOR);

            //在当前数据规模下，一次扩容耗时10ms+，要尽量避免扩容
            mStatusStack = Arrays.copyOf(mStatusStack, mStackSize);
            mIdStack = Arrays.copyOf(mIdStack, mStackSize);
            if (mContext.isDebug()) {
                Log.i(TAG, "扩容，新容量 = " + mStackSize + ",扩容耗时 = " + (System.currentTimeMillis() - start));
            }
        }
    }

    @Override
    public void onFrameFinish(long frameIntervalNanos, long frameDurationNanos) {
        if (mCurMode == RecordMode.BOOT_MODE) {
            return;
        }
        if (mMaybeHaveDirtyData) {
            mMaybeHaveDirtyData = false;
            discard();
            return;
        }
        if (frameDurationNanos >= mFrameTimeThreshold) {
            if (mContext.isDebug()) {
                Log.i(TAG, "onFrameFinish，need dump，frameDurationNanos = " + frameDurationNanos);
            }
            dump(frameDurationNanos);
        } else {
            discard();
        }
    }

}

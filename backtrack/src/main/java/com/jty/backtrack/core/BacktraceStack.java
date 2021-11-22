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
 * 记录方法状态的数组：long类型，64位，第一位表示入栈还是出栈，后63位表示时间（精确到微秒）
 * 记录方法id的数组：int类型，保存方法id
 * 所以，一个方法入栈占用96位，12字节。
 */
class BacktraceStack implements FrameMonitor.FrameObserver {
    /**
     * 初始堆栈大小
     * todo:需要计算一帧大概会有多少的深度。然后考虑到丢帧和ANR的情况，计算一下5秒需要多少深度
     */
    private static final int STACK_SIZE = 1024 * 1000;
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

    public BacktraceStack(BacktrackContext context, long frameTimeThreshold) {
        mContext = context;
        mFrameTimeThreshold = frameTimeThreshold;
        mStackSize = STACK_SIZE;
        mStatusStack = new long[mStackSize];
        mIdStack = new int[mStackSize];
    }

    protected void record(int methodId, boolean isStart) {
        checkExpand();
        long time = System.nanoTime() / 1000; //纳秒转微秒
        mStatusStack[mPoint] = StatusSpec.makeStatusSpec(isStart, time);
        mIdStack[mPoint] = methodId;
        mPoint++;
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
        mContext.getOutputProcessor().saveBacktraceStack(frameDurationNanos, dumpStatusStack, dumpIdStack);
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

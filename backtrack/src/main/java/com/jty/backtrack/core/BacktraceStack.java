package com.jty.backtrack.core;

import com.jty.backtrack.frame_monitor.FrameMonitor;
import com.jty.backtrack.utils.StatusSpec;

/**
 * @author jty
 * @date 2021/10/30
 * <p>
 * 回溯堆栈
 * 线程安全，只考虑主线程
 */
class BacktraceStack implements FrameMonitor.FrameObserver {
    /**
     * 初始堆栈大小
     */
    private static final int STACK_SIZE = 1024 * 1000;
    /**
     * 扩容因子，扩容后的容量是当前容量的 x倍
     */
    private static final float EXPAND_FACTOR = 1.5f;
    /**
     * 记录方法状态
     * 第一位1表示进栈，0表示出栈。剩下63位表示时间，微秒单位
     */
    private long[] mStatusStack = new long[STACK_SIZE];

    /**
     * 记录方法id
     */
    private int[] mIdStack = new int[STACK_SIZE];

    /**
     * 堆栈有效数据指针
     */
    private int mPoint = 0;


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
    private void dump() {
        //todo:
        discard();
    }

    /**
     * 检测是否需要扩容
     */
    private void checkExpand() {
        //todo:耗时分析
    }

    @Override
    public void onFrameFinish(long frameIntervalNanos, long frameDurationNanos) {

    }
}

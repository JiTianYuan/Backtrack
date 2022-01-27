package com.jty.backtrack.core;

import android.text.TextUtils;

/**
 * @author jty
 * @date 2021/11/2
 */
public class Config {

    //输出文件夹
    private String mOutputDir;

    //丢帧阈值，丢帧大于n 就记录
    private int mJankFrameThreshold;

    //初始栈大小
    private int mInitialStackSize;

    // 记录启动耗时
    private boolean mRecordStartUp;

    private boolean mDebuggable;

    private Config() {
    }

    public String getOutputDir() {
        return mOutputDir == null ? "" : mOutputDir;
    }

    public void setOutputDir(String outputDir) {
        this.mOutputDir = outputDir == null ? "" : outputDir;
    }

    public int getJankFrameThreshold() {
        return mJankFrameThreshold;
    }

    public int getInitialStackSize() {
        return mInitialStackSize;
    }

    public boolean isDebuggable() {
        return mDebuggable;
    }

    public boolean isRecordStartUp() {
        return mRecordStartUp;
    }

    private Config(String outputDir, int jankFrameThreshold, int initialStackSize,
                   boolean recordStartUp, boolean debuggable) {
        mOutputDir = outputDir;
        mJankFrameThreshold = jankFrameThreshold;
        mInitialStackSize = initialStackSize;
        mRecordStartUp = recordStartUp;
        mDebuggable = debuggable;
    }

    public static class Builder {

        private String outputDir = "";
        private int jankFrameThreshold = 1;
        private int initialStackSize = BacktraceStack.DEFAULT_STACK_SIZE;
        private boolean debuggable = false;
        private boolean recordStartUp = false;

        /**
         * 输出文件夹
         */
        public Builder outputDir(String outputDir) {
            if (TextUtils.isEmpty(outputDir)) {
                throw new IllegalArgumentException("outputDir is null!");
            }
            this.outputDir = outputDir;
            return this;
        }

        /**
         * 丢帧阈值，丢帧大于n 就记录
         */
        public Builder jankFrameThreshold(int jankFrameThreshold) {
            if (jankFrameThreshold <= 0) {
                throw new IllegalArgumentException("jankFrameThreshold must more than 0!");
            }
            this.jankFrameThreshold = jankFrameThreshold;
            return this;
        }

        public Builder debuggable(boolean debuggable) {
            this.debuggable = debuggable;
            return this;
        }

        /**
         * 是否开启 启动耗时记录模式
         * 需要配合 @BootEndTag 使用
         */
        public Builder recordStartUp(boolean recordStartUp) {
            this.recordStartUp = recordStartUp;
            return this;
        }

        /**
         * 初始栈大小
         * 一个栈深度占用12字节，默认栈大小 1024x1024，占用12M内存
         *
         * @see BacktraceStack#DEFAULT_STACK_SIZE
         */
        public Builder initialStackSize(int initialStackSize) {
            this.initialStackSize = initialStackSize;
            return this;
        }

        public Config build() {
            return new Config(outputDir, jankFrameThreshold, initialStackSize, recordStartUp, debuggable);
        }
    }
}

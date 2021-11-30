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

    public boolean isDebuggable(){
        return mDebuggable;
    }

    private Config(String outputDir, int jankFrameThreshold, boolean debuggable) {
        this.mOutputDir = outputDir;
        this.mJankFrameThreshold = jankFrameThreshold;
        mDebuggable = debuggable;
    }

    public static class Builder {

        private String outputDir = "";
        private int jankFrameThreshold = 1;
        private boolean debuggable = false;

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

        public Config build() {
            return new Config(outputDir, jankFrameThreshold, debuggable);
        }
    }
}

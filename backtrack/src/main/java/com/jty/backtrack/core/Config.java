package com.jty.backtrack.core;

/**
 * @author jty
 * @date 2021/11/2
 */
public class Config {
    //输出文件夹
    private String mOutputDir;

    //丢帧阈值，丢帧大于n 就记录
    private int mJankFrameThreshold;

    private Config() {
    }

    public String getOutputDir() {
        return mOutputDir == null ? "" : mOutputDir;
    }

    public int getJankFrameThreshold() {
        return mJankFrameThreshold;
    }

    private Config(String outputDir, int jankFrameThreshold) {
        this.mOutputDir = outputDir;
        this.mJankFrameThreshold = jankFrameThreshold;
    }

    public static class Builder {

        private String outputDir = "";
        private int jankFrameThreshold = 1;

        /**
         * 输出文件夹
         */
        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        /**
         * 丢帧阈值，丢帧大于n 就记录
         */
        public Builder jankFrameThreshold(int jankFrameThreshold) {
            this.jankFrameThreshold = jankFrameThreshold;
            return this;
        }

        public Config build() {
            return new Config(outputDir, jankFrameThreshold);
        }
    }
}

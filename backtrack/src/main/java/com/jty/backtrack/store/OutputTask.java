package com.jty.backtrack.store;

import android.util.Log;

import com.jty.backtrack.utils.StatusSpec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author jty
 * @date 2021/11/9
 * <p>
 * 堆栈信息输出的Task，输出文件的格式
 * @see OutputTemplate
 */
class OutputTask implements Runnable {
    private final String mOutputFilePath;
    private final long[] mDumpStatusStack;
    private final int[] mDumpIdStack;
    private final long mUIThreadId;
    private final int mProcessId;
    private final String mPkgName;
    private final boolean mDebug;


    OutputTask(String outputFilePath, long[] dumpStatusStack, int[] dumpIdStack, long uiThreadId,
               int processId, String pkgName, boolean debug) {
        mOutputFilePath = outputFilePath;
        mDumpStatusStack = dumpStatusStack;
        mDumpIdStack = dumpIdStack;
        mUIThreadId = uiThreadId;
        mProcessId = processId;
        mPkgName = pkgName;
        mDebug = debug;
    }


    @Override
    public void run() {
        if (mDebug) {
            Log.i(OutputProcessorImpl.TAG, "start output, file = " + mOutputFilePath);
        }
        OutputStreamWriter writer = null;
        BufferedWriter bw = null;
        try {
            File outFile = new File(mOutputFilePath);
            if (!outFile.exists()) {
                File parentFile = outFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                outFile.createNewFile();
            }
            OutputStream os = new FileOutputStream(outFile);
            writer = new OutputStreamWriter(os);
            bw = new BufferedWriter(writer);
            //文件头
            bw.write(OutputTemplate.buildHead(mPkgName, mProcessId, mUIThreadId));
            bw.newLine();

            //数据部分
            //时间对齐0
            long zeroTime = StatusSpec.getTime(mDumpStatusStack[0]);
            for (int i = 0; i < mDumpIdStack.length; i++) {
                long statusSpec = mDumpStatusStack[i];
                int methodId = mDumpIdStack[i];
                long time = StatusSpec.getTime(statusSpec) - zeroTime;
                long status = StatusSpec.getStatus(statusSpec);
                bw.write(OutputTemplate.buildData(methodId, time, status));
                bw.newLine();
                if (mDebug) {
                    //Log.i(OutputProcessorImpl.TAG, "写入文件 = " + OutputTemplate.buildData(methodId, time, isStart));
                }
            }

            bw.flush();
            if (mDebug) {
                Log.i(OutputProcessorImpl.TAG, "output success! file = " + mOutputFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mDebug) {
                Log.e(OutputProcessorImpl.TAG, "output " + mOutputFilePath + " is fail!!!");
                Log.e(OutputProcessorImpl.TAG, "stack: ", e);
            }
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

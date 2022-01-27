package com.jty.backtrack.store;

import com.jty.backtrack.core.BacktrackContext;
import com.jty.backtrack.core.RecordMode;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jty
 * @date 2021/10/30
 * <p>
 * 负责堆栈信息的存储
 */
public class OutputProcessorImpl implements IOutputProcessor {
    static final String TAG = "Backtrack:output";
    private final BacktrackContext mContext;
    private final ExecutorService mExecutor;


    public OutputProcessorImpl(BacktrackContext context) {
        this.mContext = context;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void saveBacktraceStack(RecordMode mode, long frameDurationNanos, long[] dumpStatusStack, int[] dumpIdStack) {
        String modeName = "_";
        switch (mode) {
            case BOOT_MODE:
                modeName = "_startUp_";
                break;
            case JANK_MODE:
                modeName = "_jank_";
                break;
        }
        String fileName = (System.currentTimeMillis()) + modeName + (frameDurationNanos / 1000000) + "ms.backtrace";
        String outputFilePath = mContext.getConfig().getOutputDir() + File.separator + fileName;
        OutputTask task = new OutputTask(outputFilePath, dumpStatusStack, dumpIdStack,
                mContext.getUIThreadId(), mContext.getProcessId(), mContext.getPkgName(), mContext.isDebug());
        mExecutor.execute(task);
    }
}

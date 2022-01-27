package com.jty.backtrack.store;

import com.jty.backtrack.core.RecordMode;

import java.util.Arrays;

/**
 * @author jty
 * @date 2021/11/9
 * <p>
 * 结果输出功能接口
 */
public interface IOutputProcessor {
    void saveBacktraceStack(RecordMode mode, long frameDurationNanos, long[] dumpStatusStack, int[] dumpIdStack);
}

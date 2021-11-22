package com.jty.backtrack.core;

import com.jty.backtrack.store.IOutputProcessor;

/**
 * @author jty
 * @date 2021/11/9
 */
public interface BacktrackContext {

    boolean isDebug();

    Config getConfig();

    IOutputProcessor getOutputProcessor();

    long getUIThreadId();

    int getProcessId();

    String getPkgName();
}

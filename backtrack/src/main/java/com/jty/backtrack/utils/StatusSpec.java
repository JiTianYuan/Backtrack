package com.jty.backtrack.utils;

/**
 * @author jty
 * @date 2021/11/4
 * <p>
 * 用于记录方法出栈入栈的时间
 * 前两位表示状态(入栈、出栈、异常)，后62位表示时间（精确到微秒）
 */
public class StatusSpec {
    private static final int FLAG_SHIFT = 62;   //前两位表示状态(入栈、出栈、异常)，后62位表示时间（精确到微秒）
    private static final long FLAG_MASK = 0x3L << FLAG_SHIFT; //1100 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000

    /**
     * 方法start
     */
    public static final long STATUS_IN = 0L << FLAG_SHIFT;

    /**
     * 方法end
     */
    public static final long STATUS_OUT = 1L << FLAG_SHIFT;

    /**
     * 方法走进了try-catch块
     */
    public static final long STATUS_EXCEPTION = 2L << FLAG_SHIFT;

    /**
     * 强制dump，会导致丢失end，所以加这个标签方便补全end
     */
    public static final long STATUS_FORCE_DUMP = 3L << FLAG_SHIFT;


    public static long getStatus(long statusSpec) {
        return (statusSpec & FLAG_MASK);
    }

    public static long getTime(long statusSpec) {
        return (statusSpec & ~FLAG_MASK);
    }


    public static long makeStatusSpec(long status, long time) {
        return (time & ~FLAG_MASK) | (status & FLAG_MASK);
    }

}

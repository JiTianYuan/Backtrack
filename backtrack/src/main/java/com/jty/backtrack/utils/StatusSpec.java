package com.jty.backtrack.utils;

/**
 * @author jty
 * @date 2021/11/4
 *
 * 用于记录方法出栈入栈的时间
 * 第一位1表示进栈，0表示出栈。剩下63位表示时间，微秒单位
 */
public class StatusSpec {
    private static final int FLAG_SHIFT = 63;   //前1位表示入栈还是出栈，后63位表示时间

    private static final long FLAG_MASK = 0x1L << FLAG_SHIFT; //1000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000

    public static boolean isStart(long statusSpec) {
        return (statusSpec & FLAG_MASK) == FLAG_MASK;
    }

    public static long getTime(long statusSpec) {
        return (statusSpec & ~FLAG_MASK);
    }


    public static long makeStatusSpec(boolean isStart, long time) {
        return (time & ~FLAG_MASK) | (isStart ? FLAG_MASK : 0L);
    }
}

package com.jty.backtrack.data_parser.worker;

/**
 * @author jty
 * @date 2021/12/21
 */
public class TraceRecordItem {
    //正常的栈
    public static final int STACK_STATUS_NORMAL = 0;

    //栈由于发生了异常，被try-catch，导致没有end
    public static final int STACK_STATUS_CATCH = 1;

    //栈因为未知异常导致没有end
    public static final int STACK_STATUS_UNKNOWN_EXCEPTION = 2;

    //栈因为强制导出导致丢失了end
    public static final int STACK_STATUS_FORCE_DUMP = 3;

    public int methodId;

    public long timeMicroseconds;

    public String status;

    /**
     * 当前堆栈的状态：
     * {@link #STACK_STATUS_NORMAL} {@link #STACK_STATUS_CATCH},{@link #STACK_STATUS_UNKNOWN_EXCEPTION}
     */
    public int stackStatus = STACK_STATUS_NORMAL;

    public TraceRecordItem() {
    }

    public TraceRecordItem(int methodId, long timeMicroseconds, String status) {
        this.methodId = methodId;
        this.timeMicroseconds = timeMicroseconds;
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + status + "," + methodId + "," + timeMicroseconds + "]";
    }
}

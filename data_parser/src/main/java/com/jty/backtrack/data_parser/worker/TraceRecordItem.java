package com.jty.backtrack.data_parser.worker;

/**
 * @author jty
 * @date 2021/12/21
 */
class TraceRecordItem {
    public int methodId;

    public long timeMicroseconds;

    public String status;

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

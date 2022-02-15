package com.jty.backtrack.data_parser.worker;

/**
 * @author jty
 * @date 2021/12/2
 */
public class Result {
    boolean success;
    String msg = "unknown";

    @Override
    public String toString() {
        if (success) {
            return "Result = " + success;
        } else {
            return "Result = " + success + ", msg = " + msg;
        }
    }
}

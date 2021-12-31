package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.List;

/**
 * @author jty
 * @date 2021/12/23
 */
abstract class BaseCorrectTask {


    protected abstract void run(List<TraceRecordItem> data);


    protected void printStack(List<List<TraceRecordItem[]>> stack) {
        System.out.println("-------------调用栈--------------");
        for (int i = 0; i < stack.size(); i++) {
            List<TraceRecordItem[]> line = stack.get(i);
            StringBuilder sb = new StringBuilder();
            for (TraceRecordItem[] method : line) {
                sb.append("|");
                sb.append(method[0].toString());
                sb.append("---");
                if (method[1] != null) {
                    sb.append(method[1].toString());
                } else {
                    sb.append("[空]");
                }

            }
            sb.append("|");
            System.out.println(sb.toString());
        }
        System.out.println("-------------——————--------------");
    }
}

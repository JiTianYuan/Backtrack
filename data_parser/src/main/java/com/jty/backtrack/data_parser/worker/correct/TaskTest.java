package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/23
 */
class TaskTest {
    public static void main(String[] args) {
        //模拟数据
        List<TraceRecordItem> data = new ArrayList<>();
        data.add(new TraceRecordItem(1, 1, "B"));
        data.add(new TraceRecordItem(2, 2, "B"));
        data.add(new TraceRecordItem(3, 3, "B"));
        data.add(new TraceRecordItem(4, 4, "B"));
        data.add(new TraceRecordItem(4, 5, "E"));
        data.add(new TraceRecordItem(3, 6, "E"));
        data.add(new TraceRecordItem(2, 7, "E"));
        data.add(new TraceRecordItem(5, 8, "B"));
        data.add(new TraceRecordItem(6, 9, "B"));
        data.add(new TraceRecordItem(7, 10, "B"));
        data.add(new TraceRecordItem(8, 11, "B"));
        data.add(new TraceRecordItem(8, 12, "E"));
        data.add(new TraceRecordItem(7, 13, "E"));
        data.add(new TraceRecordItem(9, 14, "B"));
        data.add(new TraceRecordItem(5, 15, "T"));
        data.add(new TraceRecordItem(5, 16, "E"));
        data.add(new TraceRecordItem(1, 17, "E"));

        for (TraceRecordItem item : data) {
            System.out.println(item);
        }

        RepairEndTask task = new RepairEndTask();
        task.run(data);
        System.out.println("=================================");
        for (TraceRecordItem item : data) {
            System.out.println(item + (item.stackStatus == 0 ? "" : "栈异常" + item.stackStatus));
        }
    }
}

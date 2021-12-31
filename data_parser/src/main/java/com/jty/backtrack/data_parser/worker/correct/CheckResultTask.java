package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;
import com.jty.backtrack.data_parser.worker.correct.BaseCorrectTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/23
 * <p>
 * 用来校验修正效果
 */
public class CheckResultTask extends BaseCorrectTask {
    private HashMap<Integer, List<TraceRecordItem>> map = new HashMap<>();

    @Override
    protected void run(List<TraceRecordItem> data) {
        for (TraceRecordItem item : data) {
            if (map.get(item.methodId) == null) {
                map.put(item.methodId, new ArrayList<>());
            }
            map.get(item.methodId).add(item);
        }

        for (Integer id : map.keySet()) {
            List<TraceRecordItem> items = map.get(id);
            if ((items.size() % 2) != 0) {
                System.out.println("有问题的id = " + id + "，size = " + items.size());
            }
        }
    }
}

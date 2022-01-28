package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/30
 */
public class TraceCorrector {

    public void correct(List<TraceRecordItem> data) {
        //处理 因异常导致的 没有 End事件
        DealExceptionTask dealExceptionTask = new DealExceptionTask();
        dealExceptionTask.run(data);

        //删除T事件
        RemoveTargetTask removeTTask = new RemoveTargetTask("T");
        removeTTask.run(data);

        if (needRepairEnd(data)) {
            //处理其他情况产生的 无 End事件
            RepairEndTask repairEndTask = new RepairEndTask();
            repairEndTask.run(data);
        }
        //删除F事件
        RemoveTargetTask removeFTask = new RemoveTargetTask("F");
        removeFTask.run(data);

        CheckResultTask checkResultTask = new CheckResultTask();
        checkResultTask.run(data);
    }


    public boolean needRepairEnd(List<TraceRecordItem> data) {
        HashMap<Integer, List<TraceRecordItem>> map = new HashMap<>();
        for (TraceRecordItem item : data) {
            if (map.get(item.methodId) == null) {
                map.put(item.methodId, new ArrayList<>());
            }
            map.get(item.methodId).add(item);
        }

        for (Integer id : map.keySet()) {
            List<TraceRecordItem> items = map.get(id);
            if ((items.size() % 2) != 0) {
                return true;
            }
        }
        return false;
    }
}

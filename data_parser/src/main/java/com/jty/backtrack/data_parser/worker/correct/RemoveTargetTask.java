package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.Iterator;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/30
 *
 * 移除T事件
 */
class RemoveTargetTask extends BaseCorrectTask{
    private String mTarget;

    public RemoveTargetTask(String target) {
        mTarget = target;
    }

    @Override
    protected void run(List<TraceRecordItem> data) {
        Iterator<TraceRecordItem> iterator = data.iterator();
        while (iterator.hasNext()){
            TraceRecordItem item = iterator.next();
            if (item.status.equals(mTarget)){
                iterator.remove();
            }
        }
    }
}

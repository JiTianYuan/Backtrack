package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.Iterator;
import java.util.List;

/**
 * @author jty
 * @date 2021/12/23
 *
 * 空修正器，用来对比修正效果
 */
public class EmptyCorrector extends BaseCorrector{
    @Override
    public void correct(List<TraceRecordItem> data) {
        Iterator<TraceRecordItem> iterator = data.iterator();
        while (iterator.hasNext()){
            TraceRecordItem item = iterator.next();
            if (item.status.equals("T")){
                iterator.remove();
            }
        }
    }
}

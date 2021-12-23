package com.jty.backtrack.data_parser.worker.correct;

import com.jty.backtrack.data_parser.worker.TraceRecordItem;

import java.util.List;

/**
 * @author jty
 * @date 2021/12/23
 */
public interface ICorrector {

    void correct(List<TraceRecordItem> data);
}

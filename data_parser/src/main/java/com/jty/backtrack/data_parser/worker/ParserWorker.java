package com.jty.backtrack.data_parser.worker;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;

/**
 * @author jty
 * @date 2021/11/30
 */
public class ParserWorker {

    private final ExecutorService mExecutor;

    public ParserWorker() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public void work(String mappingDir, String traceDir, String outDir, WorkListener listener) {
        //读取mapping文件
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Result result = parse(mappingDir, traceDir, outDir);
                //切换到UI线程更新UI
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (result.success) {
                            listener.onSuccess();
                        } else {
                            listener.onError(result.msg);
                        }

                    }
                });
            }
        });
    }


    private Result parse(String mappingDir, String traceDir, String outDir) {
        //加载mapping文件
        MappingReader mappingReader = new MappingReader();
        HashMap<Integer, String> mapping = mappingReader.loadMapping(mappingDir);

        //解析trace文件
        TraceConverter traceConverter = new TraceConverter(mapping);
        Result result = traceConverter.convert2Trace(traceDir, outDir);

        return result;
    }


    public interface WorkListener {
        void onSuccess();

        void onError(String msg);
    }


}

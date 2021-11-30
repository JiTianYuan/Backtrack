package com.jty.backtrack.data_parser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;

/**
 * @author jty
 * @date 2021/11/30
 */
class ParserWorker {

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
        Result result = new Result();
        MappingReader mappingReader = new MappingReader();
        mappingReader.loadMapping(mappingDir);

        return result;
    }

    private class Result {
        boolean success;
        String msg = "unknown";
    }


    public interface WorkListener {
        void onSuccess();

        void onError(String msg);
    }


}

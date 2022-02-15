package com.jty.backtrack.data_parser;

import com.jty.backtrack.data_parser.worker.ParserWorker;
import com.jty.backtrack.data_parser.worker.Result;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * @author jty
 * @date 2022/2/15
 */
class TestMain {
    public static void main(String[] args) {
        String mappingPath = "/Users/jty/Desktop/mapping";  //mapping文件夹的全路径
        String tracePath = "/Users/jty/Desktop/trace";      //trace文件夹的全路径
        String outPath = "/Users/jty/Desktop/out";          //out文件夹的全路径
        ParserWorker parserWorker = new ParserWorker();
        Result result = parserWorker.parse(mappingPath, tracePath, outPath);
        System.out.println(result);
    }
}

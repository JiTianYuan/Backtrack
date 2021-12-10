package com.jty.backtrack.data_parser;

import com.jty.backtrack.data_parser.worker.ParserWorker;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class Controller {
    @FXML
    public Pane progress;
    @FXML
    public TextField mapping_path;
    @FXML
    public TextField trace_path;
    @FXML
    public TextField out_path;
    @FXML
    public Button select_file_mapping;
    @FXML
    public Button select_file_trace;
    @FXML
    public Button select_file_out;
    @FXML
    public Button btn_go;

    private Config mConfig = new Config();

    //Window
    private Stage mStage;
    private ParserWorker mParserWorker;


    public void init(Stage stage) {
        mStage = stage;
        mParserWorker = new ParserWorker();
        //获取历史配置
        loadConfig();
        //设置loading窗
        //颜色格式：RGBA
        BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#000000A6"), null, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        progress.setBackground(background);
        progress.setVisible(false);
    }

    private void loadConfig() {
        //mapping路径
        String savedMappingPath = mConfig.get(mapping_path.getId(), "");
        if (savedMappingPath != null) {
            mapping_path.setText(savedMappingPath);
        }
        //trace路径
        String savedTracePath = mConfig.get(trace_path.getId(), "");
        if (savedTracePath != null) {
            trace_path.setText(savedTracePath);
        }
        //输出路径
        String savedOutPath = mConfig.get(out_path.getId(), "");
        if (savedOutPath != null) {
            out_path.setText(savedOutPath);
        }
    }


    private void saveConfig() {
        mConfig.set(mapping_path.getId(), mapping_path.getText());
        mConfig.set(trace_path.getId(), trace_path.getText());
        mConfig.set(out_path.getId(), out_path.getText());
        mConfig.save();
    }

    /**
     * 调起系统文件浏览器选择文件
     */
    private void chooseFile(TextField textField) {
        if (textField == null) {
            return;
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("请选择文件夹");
        File file = directoryChooser.showDialog(mStage);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * btn点击事件
     */
    public void selectMappingFile() {
        chooseFile(mapping_path);
    }

    /**
     * btn点击事件
     */
    public void selectTraceFile() {
        chooseFile(trace_path);
    }

    /**
     * btn点击事件
     */
    public void selectOutFile() {
        chooseFile(out_path);
    }

    /**
     * btn点击事件
     */
    public void onClickGo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }).start();
        progress.setVisible(true);
        mParserWorker.work(mapping_path.getText(), trace_path.getText(), out_path.getText(), new ParserWorker.WorkListener() {
            @Override
            public void onSuccess() {
                progress.setVisible(false);
                Alert alert = new Alert(Alert.AlertType.NONE, "完成",
                        new ButtonType("关闭", ButtonBar.ButtonData.YES));
                alert.setTitle("result");
                alert.showAndWait();
            }

            @Override
            public void onError(String msg) {
                progress.setVisible(false);
            }
        });


    }

}

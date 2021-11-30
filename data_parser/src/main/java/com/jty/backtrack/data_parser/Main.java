package com.jty.backtrack.data_parser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL location = getClass().getResource("/fxml/javafx.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Backtrack Data Parser");
        primaryStage.setScene(new Scene(root, 400, 260));
        primaryStage.setResizable(false);
        primaryStage.show();
        Controller controller = fxmlLoader.getController();
        controller.init(primaryStage);

    }


    public static void main(String[] args) {
        launch(args);
    }
}

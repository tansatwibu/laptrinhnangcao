package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/demo/view/Login.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/demo/style/app.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Đăng nhập - QT&VT");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

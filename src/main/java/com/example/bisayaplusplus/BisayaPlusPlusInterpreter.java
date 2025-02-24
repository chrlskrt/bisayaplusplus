package com.example.bisayaplusplus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BisayaPlusPlusInterpreter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BisayaPlusPlusInterpreter.class.getResource("interpreter-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
        InterpreterController ic = fxmlLoader.getController();
        ic.setStage(stage);
        stage.setTitle("Bisaya++ Interpreter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
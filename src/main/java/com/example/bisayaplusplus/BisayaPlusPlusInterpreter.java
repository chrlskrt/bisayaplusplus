/* BISAYAPLUSPLUS INTERPRETER APPLICATION
 * This is the main entry point for the Bisaya++ Interpreter JavaFX application.
 * It extends the Application class and is responsible for initializing and
 * displaying the main interpreter window.
 *
 * The `start` method loads the FXML layout for the interpreter view, sets up
 * the scene, configures the stage (window title), and makes it visible to the user.
 * It also retrieves the controller associated with the FXML and provides the
 * stage reference to it. The `main` method launches the JavaFX application.
 */

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
        ic.initialize();
    }

    public static void main(String[] args) {
        launch();
    }
}
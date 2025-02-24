package com.example.bisayaplusplus;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterpreterController {
    public TextArea taInput;
    public TextArea taOutput;
    List<String> inputProgram = new ArrayList<>();
    private Stage stage;

    public void runInterpreter(ActionEvent actionEvent) {
        inputProgram = Arrays.asList(taInput.getText().split("\n")); // splitting the input by line
        for (String s : inputProgram) {
            taOutput.appendText(s + "\n");
        }
    }

    public void openFile(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        // File to open will be filtered to .txt
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        // OpenDialog - choosing file
        // selectedFile - variable to store the selected file
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null){
            readFile(selectedFile);
        }
    }

    private void readFile(File file) {
        // Clearing input area
        taInput.clear();

        // Reading content of file
        List<String> content = null;
        try {
            content = Files.readAllLines(Path.of(file.toString()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read selected file.");
        }

        // Showing the content of the file
        for (String line : content){
            taInput.appendText(line + "\n");
        }
    }

    // ignore
    void setStage(Stage stage){
        this.stage = stage;
    }
}
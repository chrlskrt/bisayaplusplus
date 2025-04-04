package com.example.bisayaplusplus;

import com.example.bisayaplusplus.exception.LexerException;
import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.interpreter.Interpreter;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.parser.Parser;
import com.example.bisayaplusplus.parser.Stmt;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class InterpreterController {
    public TextArea taInput;
    public TextArea taOutput;
    private Stage stage;

    public void runInterpreter(ActionEvent actionEvent) {
        Lexer lexer = new Lexer(taInput.getText());
        List <Token> tokens;

        taOutput.setText("");
        try {
            tokens = lexer.scanTokens();

            for (Token t: tokens){
                taOutput.appendText(t.toString() + "\n");
            }
        } catch (LexerException e) {
            taOutput.appendText(e.getMessage());
            return;
        } catch (Exception e){
//            e.printStackTrace();
            taOutput.appendText("Lexer exception: " + e.getMessage());
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements;

        try {
            statements = parser.parse();
        } catch (ParserException e){
            taOutput.appendText(e.getMessage());
            return;
        } catch (Exception e){
//            e.printStackTrace();
            taOutput.appendText("Parser exception: " + e.getMessage());
            return;
        }

        for (Stmt stmt : statements){
            taOutput.appendText(stmt.toString() + '\n');
        }

        Interpreter interpreter = new Interpreter(statements);
        taOutput.appendText("OUTPUT:\n");
        try {
            interpreter.interpret(taOutput);
        } catch (Exception e){
//            e.printStackTrace();
            taOutput.appendText("Runtime exception: " + e.getMessage());
        }
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        // File to open will be filtered to .txt
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.bpp")
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
        List<String> content;
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

    // ux purposes
    void setStage(Stage stage){
        this.stage = stage;
    }

    public void saveFile(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Bisaya++ File");

        // Set default file name
        fileChooser.setInitialFileName("BisayaCode.bpp");

        // set extension filter
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Bisaya++ Files", "*.bpp", "*.txt")
        );

        File file = fileChooser.showSaveDialog(stage);

        if (file != null){
            try (FileWriter writer = new FileWriter(file)){
                writer.write(taInput.getText());
                System.out.println("File saved to: " + file.getAbsolutePath());
            } catch (IOException e){
                taOutput.setText("Error saving file.");
            }
        }
    }
}
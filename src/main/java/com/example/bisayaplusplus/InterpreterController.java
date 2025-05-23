/* INTERPRETER CONTROLLER
 * This class serves as the controller for the interpreter's user interface.
 * It manages the interaction between the UI elements (input TextArea, output
 * TextArea, line numbers) and the core interpreter logic.
 *
 * It handles actions such as running the interpreter on the input code,
 * loading code from a file, saving code to a file, and stopping the
 * currently running interpreter. It also takes care of synchronizing the
 * line numbers with the input text area and updating the output area with
 * results and error messages from the lexer, parser, and interpreter.
 */

package com.example.bisayaplusplus;

import com.example.bisayaplusplus.exception.LexerException;
import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.exception.TypeError;
import com.example.bisayaplusplus.interpreter.Interpreter;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.parser.AstPrinter;
import com.example.bisayaplusplus.parser.Parser;
import com.example.bisayaplusplus.parser.Stmt;
import javafx.application.Platform;
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
    public TextArea taLineNumbers;
    private Stage stage;
    private Interpreter interpreter;
    private boolean addedListener = false;

    /* Function that initializes the InterpreterController
     * mainly for the line numbers and synch scrolling of
     * the line numbers and the input text area.
     */
    public void initialize(){
        taInput.textProperty().addListener((observable, oldText, newText)-> updateLineNumbers());

        // synch scroll of input area and line numbers area
        taInput.scrollTopProperty().addListener((observable, oldText, newText)->{
            taLineNumbers.setScrollTop(newText.doubleValue());
        });

        updateLineNumbers();
    }

    public void updateLineNumbers() {
        int lines = taInput.getText().split("\n", -1).length;
        StringBuilder lineNumbers = new StringBuilder();
        taLineNumbers.clear();
        for (int i = 1; i <= lines; i++) {
            lineNumbers.append(i).append('\n');
        }
        taLineNumbers.setText(lineNumbers.toString());
        taLineNumbers.setScrollTop(taInput.getScrollTop());
    }

    public void runInterpreter(ActionEvent actionEvent) {
        taOutput.clear();
        
        /* Lexer */
        Lexer lexer = new Lexer(taInput.getText());
        List <Token> tokens;

        try {
            tokens = lexer.scanTokens();
        } catch (LexerException e) {
            taOutput.setText(e.getMessage() + "\n");
            return;
        } catch (Exception e){
            taOutput.setText("Lexer exception: " + e.getMessage() + "\n");
            e.printStackTrace();
            return;
        }

        /* Parser */
        Parser parser = new Parser(tokens);
        List<Stmt> statements;

        try {
            statements = parser.parse();
        } catch (ParserException e){
            taOutput.setText(e.getMessage() + "\n");
            return;
        } catch (Exception e){
            taOutput.setText("Parser exception: " + e.getMessage() + "\n");
            e.printStackTrace();
            return;
        }

        /* Interpreter */
        interpreter = new Interpreter(statements, addedListener, taOutput);
        addedListener = true;

        Thread interpreterThread = new Thread(() -> {
            try {
                interpreter.interpret();
                taOutput.appendText("\n\nProgram Finished! No error!");
            } catch (RuntimeError | TypeError e) {
                Platform.runLater(() -> {
                    taOutput.appendText(e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    taOutput.appendText("RuntimeError: " + e.getMessage());
                });
            }
        });

        interpreterThread.setDaemon(true);
        interpreterThread.start();
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

    public void stopInterpreter(ActionEvent actionEvent) {
        if (interpreter != null){
            interpreter.stopInterpreting();
            taOutput.appendText("\nExecution stopped.");
        }
    }
}
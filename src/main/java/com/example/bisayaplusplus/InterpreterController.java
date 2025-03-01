package com.example.bisayaplusplus;

import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.parser.AstPrinter;
import com.example.bisayaplusplus.parser.Expr;
import com.example.bisayaplusplus.parser.Parser;
import com.example.bisayaplusplus.parser.Stmt;
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

public class InterpreterController implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public TextArea taInput;
    public TextArea taOutput;
    List<String> inputProgram = new ArrayList<>();
    private Stage stage;

    public void runInterpreter(ActionEvent actionEvent) {
//        inputProgram = Arrays.asList(taInput.getText().split("\n")); // splitting the input by line
//        for (String s : inputProgram) {
//            taOutput.appendText(s + "\n");
//        }

        Lexer lexer = new Lexer(taInput.getText());
        List <Token> tokens;

        taOutput.setText("");
        try {
            tokens = lexer.scanTokens();

            for (Token t: tokens){
                taOutput.appendText(t.toString() + "\n");
            }
        } catch (Exception e){
            taOutput.setText(e.getMessage());
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements;

        try {
            statements = parser.parse();
        } catch (ParserException e){
            taOutput.setText(e.getMessage());
            return;
        }

        taOutput.clear();
//        taOutput.setText(new AstPrinter().print(expression));

//        for (Stmt stmt : statements){
//            taOutput.appendText(stmt.toString());
//        }

        interpret(statements);
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

    void interpret(List<Stmt> statements){
        try {
            for (Stmt stmt : statements){
                execute(stmt);
            }
        } catch (RuntimeError error){
            taOutput.setText(error.getMessage());
            return;
        }
    }

    // INTERPRETING EXPRESSIONS
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.getTokenType()){
            case MINUS: return (double) left - (double) right;
            case DIVIDE:
                checkNumberOperands(expr.operator, left, right);
                return (double) left /  (double) right;
            case MULTIPLY:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double){
                    return (double)left + (double)right;
                }

                if (left instanceof Integer && right instanceof Integer){
                    return (int)left + (int)right;
                }

                if (left instanceof String && right instanceof String){
                    return (String)left + (String) right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case AMPERSAND: return left.toString() + right.toString();
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESSER_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESSER_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case NOT_EQUAL: return !(left == right);
            case EQUAL: return (left == right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.getTokenType()){
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case LOGIC_NOT:
                return !isTruthy(right);
        }

        return null;
    }

    private boolean isTruthy(Object object){
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private void checkNumberOperand(Token operator, Object operand){
        if (operand instanceof Double || operand instanceof Integer) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if ((left instanceof Double && right instanceof Double) || (left instanceof Integer && right instanceof Integer)) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // INTERPRETING STATEMENTS
    public void execute(Stmt stmt){
        stmt.accept(this);
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(value.toString());
        taOutput.appendText(value.toString() + '\n');
        return null;
    }
}
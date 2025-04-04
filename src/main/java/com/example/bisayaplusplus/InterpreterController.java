package com.example.bisayaplusplus;

import com.example.bisayaplusplus.exception.LexerException;
import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.exception.TypeError;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.lexer.TokenType;
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
import java.util.List;
import java.util.Objects;

public class InterpreterController implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public TextArea taInput;
    public TextArea taOutput;
    List<String> inputProgram = new ArrayList<>();
    private Stage stage;
    private Environment environment;

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
        } catch (LexerException e) {
            taOutput.appendText(e.getMessage());
            return;
        } catch (Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
            taOutput.appendText("Parser exception: " + e.getMessage());
            return;
        }

//        taOutput.clear();
//        taOutput.setText(new AstPrinter().print(expression));

        for (Stmt stmt : statements){
            taOutput.appendText(stmt.toString() + '\n');
        }
        environment = new Environment();

        taOutput.appendText("OUTPUT:\n");
        try {
            interpret(statements);
        } catch (Exception e){
//            e.printStackTrace();
            taOutput.appendText("Runtime exception: " + e.getMessage());
        }

        environment.print();
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

    // ux purposes
    void setStage(Stage stage){
        this.stage = stage;
    }

    // function for interpreting
    // goes through all the statements to interpret
    void interpret(List<Stmt> statements){
        try {
            for (Stmt stmt : statements){
                execute(stmt);
            }
        } catch (RuntimeError error){
            taOutput.appendText(error.getMessage());
            return;
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        System.out.println("visitAssignExpr value datatype: " + expr.value.getClass());
        Object value = evaluate(expr.value);
        System.out.println("assign value: " + value.getClass() + " " + value);
        String destType = environment.getType(expr.name); // class for the destination variable

//        if (!(destType.equals(valueType))){
//            if (expr.value instanceof Expr.Literal && !destType.equals("double")){
//                throw new TypeError(expr.name,  valueType, expr.name.getLiteral().toString(), destType);
//            } else {
//                // if the value is a result of an expression, or if it is a literal with a destType of double
//                value = switch (destType) {
//                    case "integer" -> ((Number) value).intValue();
//                    case "double" -> ((Number) value).doubleValue();
//                    case "string" -> value.toString();
//                    default -> value;
//                };
//            }
//        } else if (valueType.equals("boolean")){
//            if ((boolean) value){
//                value = "OO";
//            } else {
//                value = "DILI";
//            }
//        }

        value = getAdjustedValue(expr.value, value, expr.name, destType);

        environment.assign(expr.name, value);
        return value;
    }

    // INTERPRETING EXPRESSIONS
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.getTokenType()){
            case MINUS:
                checkNumberOperands(expr.operator, left, right, "SUBTRACTION");
                return ((Number) left).doubleValue() -  ((Number) right).doubleValue();
            case DIVIDE:
                checkNumberOperands(expr.operator, left, right, "DIVISION");
                System.out.println(6.6/2.2);
                return ((Number) left).doubleValue() /  ((Number) right).doubleValue();
            case MULTIPLY:
                checkNumberOperands(expr.operator, left, right, "MULTIPLICATION");
                return ((Number) left).doubleValue() *  ((Number) right).doubleValue();
            case PLUS:
                if (left instanceof Number && right instanceof Number){
                    return ((Number) left).doubleValue() +  ((Number) right).doubleValue();
                }

                if (left instanceof String && right instanceof String){
                    return (String)left + (String) right;
                }

                throw new RuntimeError(expr.operator, "ADDITION: Operands must be of the same data type.");
            case CONCAT: return left.toString() + right.toString();
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right, "COMPARISON (GREATER_THAN)");
                return ((Number) left).doubleValue() >  ((Number) right).doubleValue();
            case GREATER_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right, "COMPARISON (GREATER_OR_EQUAL)");
                return ((Number) left).doubleValue() >=  ((Number) right).doubleValue();
            case LESSER_THAN:
                checkNumberOperands(expr.operator, left, right, "COMPARISON (LESSER_THAN)");
                return ((Number) left).doubleValue() <  ((Number) right).doubleValue();
            case LESSER_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right, "COMPARISON (LESSER_OR_EQUAL)");
                return ((Number) left).doubleValue() <=  ((Number) right).doubleValue();
            case NOT_EQUAL: return !(left == right);
            case DOUBLE_EQUAL: return (left == right);
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
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if ((expr.operator).getTokenType() == TokenType.LOGIC_OR){
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.getTokenType()){
            case NEGATIVE:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer){
                    return -(Integer) right;
                } else if (right instanceof Double){
                    return -(Double) right;
                }
            case POSITIVE:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer){
                    return +(Integer) right;
                } else if (right instanceof Double){
                    return +(Double) right;
                }
            case LOGIC_NOT:
                return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
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

    private void checkNumberOperands(Token operator, Object left, Object right, String message){
//        if ((left instanceof Double && right instanceof Double) || (left instanceof Integer && right instanceof Integer)) return;
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, message + ": Operands must be numbers. " + left + " = " + left.getClass() + " ; " + right + " = " + right.getClass());
    }

    // INTERPRETING STATEMENTS
    public void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;

        try {
            this.environment = environment;

            for (Stmt stmt : statements){
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
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
        System.out.println("appending to output");
        taOutput.appendText(value.toString() + '\n');
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null){
            value = evaluate(stmt.initializer);

            final var valueDataType = getValueDataType(stmt, value);
            System.out.println(value + " : " + value.getClass() + valueDataType);

            value = getAdjustedValue(stmt.initializer, value, stmt.name, stmt.dataType);
        }

        environment.define((String) stmt.name.getLiteral(), stmt.dataType, value);
        return null;
    }

    /*
    * Returns the data type of the assigned value to a variable
    * @param stmt      - the variable declaration statement e.g. DATATYPE IDENTIFIER = INITIALIZER (NUMERO C = 5)
    * @param value     - evaluated value of the initializer. in example above, it would be 5
    */
    private String getValueDataType(Stmt.Var stmt, Object value) {
        String valueDataType;

        if (stmt.initializer instanceof Expr.Literal){
            valueDataType = ((Expr.Literal) stmt.initializer).dataType;
        } else {
            valueDataType = value.getClass().getSimpleName().toLowerCase();
        }

        return valueDataType;
    }

    /*
    * Returns the adjusted value to be assigned to a variable based on its declared data type.
    * Throws a TypeError if the value's type is incompatible with the variable's declared type.
    * @param valueExpr       - the expression representing the value to assign e.g. (4+5), 5
    * @param origValue       - original evaluated value of the expression
    * @param variable        - the token representing the variable
    * @param varDataType     - declared data type of the variable
    */
    private Object getAdjustedValue(Expr valueExpr, Object origValue, Token variable, String varDataType){
        String valueDataType = origValue.getClass().getSimpleName().toLowerCase();
        Object value = origValue;

        if (varDataType.equals("double") && valueDataType.equals("integer")){
            value = ((Number) origValue).doubleValue();
        } else if (varDataType.equals("integer") && valueDataType.equals("double") && !(valueExpr instanceof Expr.Literal)){
            value = ((Number) origValue).intValue();
        } else if (valueDataType.equals("boolean") && !(valueExpr instanceof Expr.Literal)){
            if ((boolean) origValue){
                value = "OO";
            } else {
                value = "DILI";
            }
        } else if (!varDataType.equals(valueDataType)) {
            throw new TypeError(variable, valueDataType, variable.getLiteral().toString(), varDataType);
        }

        return value;
    }
}
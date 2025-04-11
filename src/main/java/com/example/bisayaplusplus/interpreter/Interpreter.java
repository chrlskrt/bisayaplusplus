package com.example.bisayaplusplus.interpreter;

import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.exception.TypeError;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.lexer.TokenType;
import com.example.bisayaplusplus.parser.Expr;
import com.example.bisayaplusplus.parser.Stmt;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object>{
    private final List<Stmt> statements;
    private Environment environment;
    private TextArea taOutput;

    public Interpreter (List<Stmt> statements){
        this.statements = statements;
        environment = new Environment();
    }

    // function for interpreting
    // goes through all the statements to interpret
    public void interpret(TextArea taOutput){
        this.taOutput = taOutput;
        for (Stmt stmt : statements){
            execute(stmt);
        }
    }

    public void interpret(){
        for (Stmt stmt : statements){
            execute(stmt);
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        System.out.println("visitAssignExpr value datatype: " + expr.value.getClass());
        Object value = evaluate(expr.value);
        System.out.println("assign value: " + value.getClass() + " " + value);
        String valueDataType = getValueDataType(expr.value, value);
        String destType = environment.getType(expr.name); // class for the destination variable
        System.out.println(destType);
        value = getAdjustedValue(expr.value, value, expr.name, destType, valueDataType);

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
                    return left + (String) right;
                }

                throw new RuntimeError(expr.operator, "ADDITION: Operands must be of the same data type.");
            case CONCAT: return left.toString() + right.toString();
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
        TokenType opType = (expr.operator).getTokenType();
        Object right = evaluate(expr.right);

        if (opType == TokenType.LOGIC_NOT){
            return !isTruthy(right);
        }

        Object left = evaluate(expr.left);
        switch (opType){
            case LOGIC_OR: if (isTruthy(left)) return left;
            case LOGIC_AND: if (!isTruthy(left)) return left; return right;
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
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitIncrementOrDecrementExpr(Expr.IncrementOrDecrement expr) {
        Token variable = ((Expr.Variable) expr.var).name;
        Object value = environment.get(variable);
        int delta = (expr.operator.getTokenType() == TokenType.INCREMENT) ? +1 : -1;
        Object nxtVal = switch (value.getClass().getSimpleName()) {
            case "Integer" -> (Integer) value + delta;
            case "Double" -> (Double) value + delta;
            case "Character" -> (Character) value + delta;
            default -> throw new RuntimeError(variable, "Cannot " + expr.operator.getTokenType() + " this variable.");
        };

        environment.assign(variable, nxtVal);

        if (expr.isPrefix){
            return value;
        } else {
            return nxtVal;
        }
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
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, message + ": Operands must be numbers. " + left + " = " + left.getClass() + " ; " + right + " = " + right.getClass());
    }

    // INTERPRETING STATEMENTS
    public void execute(Stmt stmt){
        stmt.accept(this);
    }
    public boolean executeElIf(Stmt stmt){
        return (boolean) stmt.accept(this);
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
    public Object visitIfStmt(Stmt.If stmt) {
        boolean isIfDone = false;
        if (isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);

            isIfDone = true;
        } else if (stmt.elseIfBranch != null) {
            for (Stmt.ElseIf elif: stmt.elseIfBranch){
                if (executeElIf(elif)){
                    isIfDone = true;
                    break;
                }
            }
        }

        if (stmt.elseBranch != null && !isIfDone){
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Object visitElseIfStmt(Stmt.ElseIf stmt) {
        if (isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
            return true;
        }

        return false;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(value.toString());
        System.out.println("appending to output");

        if (taOutput != null){
            taOutput.appendText(value.toString());
        } else {
            System.out.println(value.toString() + '\n');
        }

        return null;
    }

    @Override
    public Object visitForLoopStmt(Stmt.ForLoop stmt) {

        // we combine the initialization, condition, update and body into 1 block
        Stmt newBody = new Stmt.Block(Arrays.asList(stmt.body, stmt.update));
        Stmt whileBodyBlock = new Stmt.While(stmt.condition, newBody);
        Stmt.Block forBlock = new Stmt.Block(Arrays.asList(stmt.initialization, whileBodyBlock));

        executeBlock(forBlock.statements, new Environment(environment));

        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null){
            value = evaluate(stmt.initializer);

            final var valueDataType = getValueDataType(stmt.initializer, value);
            System.out.println(value + " : " + value.getClass() + valueDataType);

            value = getAdjustedValue(stmt.initializer, value, stmt.name, stmt.dataType, valueDataType);
        }

        environment.define(stmt.name, stmt.dataType, value);
        return null;
    }

    /*
     * Returns the data type of the assigned value to a variable
     * @param valueExpr - the initializer part of variable declaration or assignment declarations e.g. DATATYPE IDENTIFIER = INITIALIZER (NUMERO C = 5)
     * @param value     - evaluated value of the initializer. in example above, it would be 5
     */
    private String getValueDataType(Expr valueExpr, Object value) {
        String valueDataType;

        if (valueExpr instanceof Expr.Literal){
            valueDataType = ((Expr.Literal) valueExpr).dataType;
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
    private Object getAdjustedValue(Expr valueExpr, Object origValue, Token variable, String varDataType, String valueDataType){
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

package com.example.bisayaplusplus.parser;

import java.util.Objects;

public class AstPrinter implements Expr.Visitor<String>{
    public String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("ASSIGN " + expr.name.getLiteral() + " with ", expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize((expr.operator).getTokenType().toString(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null ) return null;
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.getTokenType().toString(), expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize((expr.operator.getLiteral() != null) ? (String)expr.operator.getLiteral() : expr.operator.getTokenType().toString(), expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return (String) expr.name.getLiteral();
    }

    @Override
    public String visitIncrementOrDecrementExpr(Expr.IncrementOrDecrement expr) {
        return null;
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr: exprs){
            if(expr == null){
                continue;
            }
            builder.append(" ");
            builder.append(expr.accept(this));
        }

        builder.append(")");

        return builder.toString();
    }
}

package com.example.bisayaplusplus.parser;

import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.lexer.TokenType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicStampedReference;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public List<Stmt> parse() throws ParserException {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()){
            statements.add(statement());
        }
        return statements;
    }

    private Stmt statement() throws ParserException {
        if (match(TokenType.PRINT_STMT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() throws ParserException {
        if(!match(TokenType.COLON)){
            throw new ParserException((String)previous().getLiteral(), previous().getLine());
        }
        Expr value = expression();
        consume(TokenType.NEW_LINE, "Expect NEWLINE after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() throws ParserException {
        Expr expr = expression();
        consume(TokenType.NEW_LINE, "Expect '\n' after the expression");
        return new Stmt.Expression(expr);
    }

    private Expr expression() throws ParserException {
        return equality();
    }

    private Expr equality() throws ParserException {
        Expr expr = comparison();

        while (match(TokenType.NOT_EQUAL, TokenType.DOUBLE_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() throws ParserException {
        Expr expr = term();

        while (match(TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.LESSER_THAN, TokenType.LESSER_OR_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() throws ParserException {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS, TokenType.AMPERSAND, TokenType.LOGIC_OR)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() throws ParserException {
        Expr expr = unary();

        while (match(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO, TokenType.LOGIC_AND)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() throws ParserException {
        if (match(TokenType.LOGIC_NOT, TokenType.NEGATIVE, TokenType.POSITIVE)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() throws ParserException {
        if (match(TokenType.BOOL_FALSE)) return new Expr.Literal(false);
        if (match(TokenType.BOOL_TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NULL)) return new Expr.Literal(null);

        if (match(TokenType.INTEGER, TokenType.FLOAT, TokenType.STRING, TokenType.IDENTIFIER)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        Token token = peek();

        throw new ParserException("Expect expression. " +   ((token.getLiteral() == null ? token.getTokenType().toString() : token.getLiteral())), token.getLine());
    }

    private void synchronize(){
        advance();

        while (!isAtEnd()){
            if (previous().getTokenType() == TokenType.NEW_LINE) return;
            if (Lexer.keywords.containsValue(peek().getTokenType())){
                return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String message) throws ParserException {
        if (check(type)) return advance();

        Token token = peek();

        throw new ParserException(message + " " + ((token.getLiteral()) == null ? token.getTokenType().toString() : token.getLiteral()), token.getLine());
    }


    // checks if the current expr has those token types

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // checks if current token type is the same as the given type
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getTokenType() == type;
    }

    private Token advance(){
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getTokenType() == TokenType.END_STMT;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

}

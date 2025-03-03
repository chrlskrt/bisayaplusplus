package com.example.bisayaplusplus.parser;

import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.exception.RuntimeError;
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

    // main function doing the parsing
    public List<Stmt> parse() throws ParserException {
        List<Stmt> statements = new ArrayList<>();

        if (!match(TokenType.START_STMT)){
            throw new ParserException("Expected 'SUGOD' at the start of the program.", peek().getLine());
        }

        consume(TokenType.NEW_LINE, "Expected NEW_LINE after 'SUGOD");

        while (!isAtEnd()){
            statements.add(declaration());
        }

        return statements;
    }

    // function for declaration statement
    private Stmt declaration() throws ParserException {
        try {
            if (match(TokenType.CREATE_STMT)) return varDeclaration();

            return statement();
        } catch (ParserException e){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() throws ParserException {
        if (!match(TokenType.INT_TYPE, TokenType.BOOL_TYPE, TokenType.CHAR_TYPE, TokenType.FLOAT_TYPE)){
            throw new ParserException("Expected DATA_TYPE, but received " + previous().getTokenType() + " ", previous().getLine());
        }
        Token name = consume(TokenType.IDENTIFIER, "Expected IDENTIFIER after DATA_TYPE / COMMA.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)){
            initializer = expression();
        }

        consume(TokenType.NEW_LINE, "Expected NEW_LINE after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

//    private List<Stmt> varDeclaration() throws ParserException {
//        if (!match(TokenType.INT_TYPE, TokenType.CHAR_TYPE, TokenType.BOOL_TYPE, TokenType.FLOAT_TYPE)){
//            throw new ParserException("Expected DATA_TYPE, but received " + previous().getLiteral(), previous().getLine());
//        }
//
//        List<Stmt> statements = new ArrayList<>();
//
//
//        do {
//            Token name = consume(TokenType.IDENTIFIER, "Expected IDENTIFIER after DATA_TYPE / COMMA.");
//            Expr initializer = null;
//            if (match(TokenType.EQUAL)){
//                initializer = expression();
//            }
//
//            statements.add(new Stmt.Var(name, initializer));
//        } while (match(TokenType.COMMA));
//
//        consume(TokenType.NEW_LINE, "Expected NEW_LINE after variable declaration.");
//        return statements;
//    }

    // function to identify statement
    private Stmt statement() throws ParserException {
        // if it starts with IPAKITA / PRINT_STMT, it will go to the printStatement parser
        if (match(TokenType.PRINT_STMT)) return printStatement();
        if (match(TokenType.CODE_BLOCK)) return new Stmt.Block(block());

        return expressionStatement();
    }

    /*
    * PRINT STATEMENT SYNTAX
    * IPAKITA: {EXPR}
    */
    private Stmt printStatement() throws ParserException {
        consume(TokenType.COLON, "Expect ':' after IPAKITA statement.");
        Expr value = expression();
        consume(TokenType.NEW_LINE, "Expect NEWLINE after value.");
        return new Stmt.Print(value);
    }

    // for code blocks
    private List<Stmt> block() throws ParserException {
        List<Stmt> statements = new ArrayList<>();
        consume(TokenType.LEFT_BRACKET, "Expect '{' after PUNDOK statement.");

        while (!check(TokenType.RIGHT_BRACKET) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACKET, "Expect '}' after block.");
        return statements;
    }

    // PARSING EXPRESSION STATEMENTS - refers mostly to mathematical expressions
    private Stmt expressionStatement() throws ParserException {
        Expr expr = expression();
        consume(TokenType.NEW_LINE, "Expect '\n' after the expression");
        return new Stmt.Expression(expr);
    }

    // expression - equality - comparison - term - factor - unary - primary
    private Expr expression() throws ParserException {
        return assignment();
    }

    // check assigning variable value
    private Expr assignment() throws ParserException {
        Expr expr = equality();

        if (match(TokenType.EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            throw new ParserException("Invalid assignment target", equals.getLine());
        }

        return expr;
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

        if (match(TokenType.INTEGER, TokenType.FLOAT, TokenType.STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if (match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        Token token = peek();

        throw new ParserException("Expect expression. " +   ((token.getLiteral() == null ? token.getTokenType().toString() : token.getLiteral())), token.getLine());
    }

    // i d k what is thiz for
    private void synchronize() throws ParserException {
        advance();

        while (!isAtEnd()){
            if (previous().getTokenType() == TokenType.NEW_LINE) return;
            if (Lexer.keywords.containsValue(peek().getTokenType())){
                return;
            }

            advance();
        }
    }

    // function for throwing errors basically
    private Token consume(TokenType type, String message) throws ParserException {
        if (check(type)) return advance();

        Token token = peek();

        throw new ParserException(message + " " + ((token.getLiteral()) == null ? token.getTokenType().toString() : token.getLiteral()), token.getLine());
    }

    // checks if the current expr has any of those token types
    private boolean match(TokenType... types) throws ParserException {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // checks if current token type is the same as the given type
    private boolean check(TokenType type) throws ParserException {
        if (isAtEnd()) return false;
        return peek().getTokenType() == type;
    }

    // get current token, increment current
    private Token advance() throws ParserException {
        if (!isAtEnd()) current++;
        return previous();
    }

    // check if the parser has already reached the end of the program
    private boolean isAtEnd() throws ParserException {
//        if (current == tokens.size()){
//            throw new ParserException("Expected 'KATAPUSAN' at the end of the program.", previous().getLine()+1);
//        }
        return peek().getTokenType() == TokenType.END_STMT;
    }

    // get next token wo incrementing the current counter
    private Token peek() {
        return tokens.get(current);
    }

    // get previous token
    private Token previous() {
        return tokens.get(current - 1);
    }

}

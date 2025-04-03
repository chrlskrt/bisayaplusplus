package com.example.bisayaplusplus.parser;

import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.parser.AstPrinter;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private AstPrinter astPrinter = new AstPrinter();

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    // main function doing the parsing
    public List<Stmt> parse() throws ParserException {
        List<Stmt> statements = new ArrayList<>();

        // START STATEMENT
        if (!matchToken(TokenType.START_STMT)){
            throw new ParserException("Expected 'SUGOD' at the start of the program.", getCurrToken().getLine());
        }

        // there should be a new_line after SUGOD
        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after 'SUGOD'");

        // go through all the tokens after SUGOD
        try {
            while (!isCurrTokenType(TokenType.END_STMT) || isAtEnd()){
                parseDeclarations(statements);
            }
        } catch (Exception e){
            if (e instanceof ParserException){
                throw (ParserException) e;
            }

            System.err.println(e.getMessage());
        }

        if (isCurrTokenType(TokenType.END_STMT)){
            advance(); // consume KATAPUSAN keyword
        } else {
            throw new ParserException("Expected 'KATAPUSAN' at the end of the program.", getPrevToken().getLine() + 1);
        }

        System.out.println(isAtEnd());

        // checking if there's extra code after a 'KATAPUSAN' that is not a new_line character
        if (!isAtEnd()){
            while (isCurrTokenType(TokenType.NEW_LINE)){
                advance();
            }

            if (!isAtEnd()){
                System.out.println(getCurrToken());
                throw new ParserException("Unexpected code found after 'KATAPUSAN' end statement.", getCurrToken().getLine());
            }
        }

        return statements;
    }

    // function for declaration statement
    private void parseDeclarations(List<Stmt> statements) throws ParserException {
        if (matchToken(TokenType.CREATE_STMT)) {
            System.out.println("adding var dec statement");
            statements.addAll(parseVarDeclaration());
            return;
        }

        System.out.println("adding normal statement");
        statements.add(parseStatement());
    }

    // variable declaration
    private List<Stmt> parseVarDeclaration() throws ParserException {
        String dataType = switch (getCurrToken().getTokenType()) {
            case INT_TYPE -> "int";
            case BOOL_TYPE -> "boolean";
            case CHAR_TYPE -> "char";
            case DOUBLE_TYPE -> "double";
            default ->
                    throw new ParserException("Expected DATA_TYPE after 'MUGNA', but received " + getCurrToken().getTokenType() + " ", getCurrToken().getLine());
        };

        advance(); // consume data type
        System.out.println("after data type: " + getCurrToken());

        List<Stmt> varDeclarations = new ArrayList<>();

        while (!isCurrTokenType(TokenType.NEW_LINE)){
            Token name = consumeToken(TokenType.IDENTIFIER, "Expected IDENTIFIER after DATA_TYPE / COMMA.");
            Expr initializer = null;
            if (matchToken(TokenType.EQUAL)){
                System.out.println(name.getLiteral() + " has initialized value");
                initializer = parseExpression();
                System.out.println(name.getLiteral() + " is initialized with " + ((Expr.Literal) initializer).value);
            }

            varDeclarations.add(new Stmt.Var(dataType, name, initializer));

            System.out.println(getCurrToken());
            if (isCurrTokenType(TokenType.COMMA)) advance();
        }

        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after variable declaration.");
        return varDeclarations;
    }

    // function to identify statement
    private Stmt parseStatement() throws ParserException {
        // if it starts with IPAKITA / PRINT_STMT, it will go to the printStatement parser
        if (matchToken(TokenType.PRINT_STMT)) return parsePrintStatement();
        if (matchToken(TokenType.CODE_BLOCK)) return new Stmt.Block(parseBlock());

        return parseExprStatement();
    }

    /*
    * PRINT STATEMENT SYNTAX
    * IPAKITA: {EXPR}
    */
    private Stmt parsePrintStatement() throws ParserException {
        consumeToken(TokenType.COLON, "Expect ':' after IPAKITA statement.");
        Expr value = parseExpression();
        System.out.println("the value for print: " + astPrinter.print(value));
        consumeToken(TokenType.NEW_LINE, "Expect NEWLINE after value.");
        return new Stmt.Print(value);
    }

    // for code blocks
    private List<Stmt> parseBlock() throws ParserException {
        List<Stmt> blockStatements = new ArrayList<>();
        consumeToken(TokenType.LEFT_CURLY, "Expect '{' after PUNDOK statement.");

        while (!isCurrTokenType(TokenType.RIGHT_CURLY) && !isAtEnd()){
            parseDeclarations(blockStatements);
        }

        consumeToken(TokenType.RIGHT_CURLY, "Expect '}' after block.");
        return blockStatements;
    }

    // PARSING EXPRESSION STATEMENTS - refers mostly to mathematical expressions
    private Stmt parseExprStatement() throws ParserException {
        Expr expr = parseExpression();
        consumeToken(TokenType.NEW_LINE, "Expect '\n' after the expression");
        return new Stmt.Expression(expr);
    }

    // expression - equality - comparison - term - factor - unary - primary
    private Expr parseExpression() throws ParserException {
        return parseAssignment();
    }

    // check assigning variable value
    private Expr parseAssignment() throws ParserException {
        Expr expr = parseEquality();

        if (matchToken(TokenType.EQUAL)){
            System.out.println("Assignment");
            Token equals = getPrevToken();
            Expr value = parseAssignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable) expr).name;
                System.out.println(name);
                System.out.println(value);
                return new Expr.Assign(name, value);
            }

            throw new ParserException("Invalid assignment target", equals.getLine());
        }

        return expr;
    }

    private Expr parseEquality() throws ParserException {
        Expr expr = parseComparison();

        while (matchToken(TokenType.NOT_EQUAL, TokenType.DOUBLE_EQUAL)) {
            Token operator = getPrevToken();
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseComparison() throws ParserException {
        Expr expr = parseTerm();

        while (matchToken(TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.LESSER_THAN, TokenType.LESSER_OR_EQUAL)) {
            Token operator = getPrevToken();
            Expr right = parseTerm();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseTerm() throws ParserException {
        Expr expr = parseFactor();

        while (matchToken(TokenType.MINUS, TokenType.PLUS, TokenType.CONCAT, TokenType.LOGIC_OR)) {
            Token operator = getPrevToken();
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseFactor() throws ParserException {
        Expr expr = parseUnary();

        while (matchToken(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO, TokenType.LOGIC_AND)) {
            Token operator = getPrevToken();
            Expr right = parseUnary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseUnary() throws ParserException {
        if (matchToken(TokenType.LOGIC_NOT, TokenType.NEGATIVE, TokenType.POSITIVE)) {
            Token operator = getPrevToken();
            Expr right = parseUnary();
            return new Expr.Unary(operator, right);
        }

        return parsePrimary();
    }

    private Expr parsePrimary() throws ParserException {
        System.out.println("parsePrimary: " + getCurrToken().getLiteral() + getCurrToken().getTokenType());
        if (matchToken(TokenType.BOOL_FALSE)) return new Expr.Literal("boolean", false);
        if (matchToken(TokenType.BOOL_TRUE)) return new Expr.Literal("boolean", true);
        if (matchToken(TokenType.NULL)) return new Expr.Literal("null", null);
        if (matchToken(TokenType.INTEGER)) return new Expr.Literal("int", getPrevToken().getLiteral());
        if (matchToken(TokenType.DOUBLE)) return new Expr.Literal("double", getPrevToken().getLiteral());
        if (matchToken(TokenType.STRING)) return new Expr.Literal("string", getPrevToken().getLiteral());
        if (matchToken(TokenType.IDENTIFIER)) return new Expr.Variable(getPrevToken());
        if (matchToken(TokenType.LEFT_PAREN)){
            Expr expr = parseExpression();
            consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        Token token = getCurrToken();
        throw new ParserException("Expected expression not found. " +   ((token.getLiteral() == null ? token.getTokenType().toString() : token.getLiteral())), token.getLine());
    }

    // error recovery function in the parser
    private void synchronize() {
        advance();

        // Skip tokens until a valid statement start is found
        while (!isAtEnd()){
            if (getPrevToken().getTokenType() == TokenType.NEW_LINE) return; // stops at a NEW_LINE
            if (Lexer.keywords.containsValue(getCurrToken().getTokenType())){
                return; // stops at a keyword
            }

            advance(); // skip invalid tokens
        }
    }

    // function for throwing errors
    private Token consumeToken(TokenType expectedType, String message) throws ParserException {
        if (isCurrTokenType(expectedType)) return advance(); // if the token type matches, it will increment current counter

        // if the type does not match
        Token token = getCurrToken();

        // throws exception. it gives the message and the token that was found instead of the expected tokentype
        throw new ParserException(message + " " + ((token.getLiteral()) == null ? token.getTokenType().toString() : token.getLiteral()), token.getLine());
    }

    // checks if the current expr has any of those token types
    // automatically advances to the next token type if found a match
    private boolean matchToken(TokenType... types) {
        for (TokenType type : types) {
            if (isCurrTokenType(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // checks if the current token type is the same as the given type
    private boolean isCurrTokenType(TokenType type){
        if (isAtEnd()) return false;
        return getCurrToken().getTokenType() == type;
    }

    // get next token, increment current counter
    private Token advance() {
        if (!isAtEnd()) current++;
        return getPrevToken();
    }

    // get next token wo incrementing the current counter
    private Token getCurrToken() {
        return tokens.get(current);
    }

    // get previous token
    private Token getPrevToken() {
        return tokens.get(current - 1);
    }

    // check if the parser has already reached the end of the program
    private boolean isAtEnd(){
        return current == tokens.size();
    }
}

package com.example.bisayaplusplus.parser;

import com.example.bisayaplusplus.exception.ParserException;
import com.example.bisayaplusplus.lexer.Lexer;
import com.example.bisayaplusplus.lexer.Token;
import com.example.bisayaplusplus.lexer.TokenType;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final AstPrinter astPrinter = new AstPrinter();

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

        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after 'SUGOD''", true);

        String typeStmt = "";
        // go through all the tokens after SUGOD
        while (!matchToken(TokenType.END_STMT) && !isAtEnd()){
            typeStmt = parseStatements(statements);

            if (!typeStmt.equals("IF")){
                consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after a statement. 1 statement per line.", true);
            }
        }

        if (isAtEnd() && getPrevToken().getTokenType() != TokenType.END_STMT){
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
    private String parseStatements(List<Stmt> statements) throws ParserException {
        // Variable declarations
        if (matchToken(TokenType.CREATE_STMT)) {
            statements.addAll(parseVarDeclaration());
            return "CREATE";
        }

        // Print statement
        if (matchToken(TokenType.PRINT_STMT)){
            statements.add(parsePrintStatement());
            return "PRINT";
        }

        // If statement
        if (matchToken(TokenType.IF)) {
            statements.add(parseIfStmt());
            return "IF";
        }

        statements.add(parseExprStatement());
        return "EXPR";
    }

    // variable declaration
    private List<Stmt> parseVarDeclaration() throws ParserException {
        String dataType = switch (getCurrToken().getTokenType()) {
            case INT_KEYWORD -> "integer";
            case BOOL_KEYWORD -> "boolean";
            case CHAR_KEYWORD -> "character";
            case DOUBLE_KEYWORD -> "double";
            default ->
                    throw new ParserException("Expected DATA_TYPE after 'MUGNA', but received " + getCurrToken().getTokenType() + " \"" + getCurrToken().getLiteral() + "\"", getCurrToken().getLine());
        };

        advance(); // consume data type

        List<Stmt> varDeclarations = new ArrayList<>();

//        while (!isAtEnd() && !isCurrTokenType(TokenType.NEW_LINE)){
//            Token name = consumeToken(TokenType.IDENTIFIER, "Expected IDENTIFIER after DATA_TYPE / COMMA.");
//            Expr initializer = null;
//            if (matchToken(TokenType.EQUAL)){
//                System.out.println(name.getLiteral() + " has initialized value");
//                initializer = parseExpression();
//                System.out.println(name.getLiteral() + " is initialized with " + astPrinter.print(initializer));
//            }
//
//            varDeclarations.add(new Stmt.Var(dataType, name, initializer));
//
//            System.out.println(getCurrToken());
//            if (isCurrTokenType(TokenType.COMMA)) advance();
//        }
        do {
            Token name = consumeToken(TokenType.IDENTIFIER, "Expected IDENTIFIER after DATA_TYPE / COMMA.", false);
            Expr initializer = null;
            if (matchToken(TokenType.EQUAL)){
                System.out.println(name.getLiteral() + " has initialized value");
                initializer = parseExpression();
                System.out.println(name.getLiteral() + " is initialized with " + astPrinter.print(initializer));
            }

            varDeclarations.add(new Stmt.Var(dataType, name, initializer));
        } while (matchToken(TokenType.COMMA));

        return varDeclarations;
    }

    private Stmt parseIfStmt() throws ParserException {
        System.out.println("parsing if statement");
        consumeToken(TokenType.LEFT_PAREN, "Expected '(' after IF keyword.", false);
        System.out.println("parsing condition");
        Expr condition = parseExpression();
        checkIFConditions(condition);
        System.out.println("consuming right paren");
        consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after the IF condition.", false);
        System.out.println("consuming new line");
        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after the IF statement. To parse PUNDOK for IF.", false);

        // for if block statements
        Stmt thenBranch = new Stmt.Block(parseBlock("IF BLOCK"));
        List<Stmt.ElseIf> elseIfBranch = null;
        Stmt elseBranch = null;

        if (matchToken(TokenType.IF_ELSE)){
            elseIfBranch = new ArrayList<>();
            do {
                // getting condition
                consumeToken(TokenType.LEFT_PAREN, "Expected '(' after ELSE_IF keyword.", false);
                Expr elIfCondition = parseLogicalOR();
                consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after the ELSE_IF condition.", false);
                consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after the ELSE_IF statement. Only 1 statement per line.", false);

                // storing and getting executable statements under el-if block
                elseIfBranch.add(new Stmt.ElseIf(elIfCondition, new Stmt.Block(parseBlock("ELSE-IF-"+(elseIfBranch.size() + 1)))));
            } while (matchToken(TokenType.IF_ELSE));
        }

        if (matchToken(TokenType.ELSE)){
            consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after ELSE statement. Only 1 statement per line.", false);
            elseBranch = new Stmt.Block(parseBlock("ELSE BLOCK"));
        }

        if (matchToken(TokenType.ELSE)) throw new ParserException("You can only have 1 else block per if-else statements.", getPrevToken().getLine());

        return new Stmt.If(condition, thenBranch, elseIfBranch, elseBranch);
    }

    private void checkIFConditions(Expr condition) throws ParserException {
        if (!(condition instanceof Expr.Logical)){
            Expr expr = condition;
            while (expr instanceof Expr.Grouping){
                expr = ((Expr.Grouping) expr).expression;
            }

            if (!(expr instanceof Expr.Logical)){
                if (expr instanceof Expr.Assign){
                    throw new ParserException("Invalid IF condition. Maybe you meant \"==\" instead of \"=\"?", getPrevToken().getLine());
                }

                throw new ParserException("Invalid IF condition. Expected BOOLEAN expression.", getPrevToken().getLine());
            }
        }
    }
    /*
    * PRINT STATEMENT SYNTAX
    * IPAKITA: {EXPR}
    */
    private Stmt parsePrintStatement() throws ParserException {
        consumeToken(TokenType.COLON, "Expect ':' after IPAKITA statement.", false);
        Expr value = parseExpression();
        System.out.println("the value for print: " + astPrinter.print(value));
        return new Stmt.Print(value);
    }

    // for code blocks - code sulod sa PUNDOK {}
    private List<Stmt> parseBlock(String blockName) throws ParserException {
        consumeToken(TokenType.CODE_BLOCK, "Expected 'PUNDOK' statement for the " + blockName + " block.", false);
        List<Stmt> blockStatements = new ArrayList<>();
        consumeToken(TokenType.LEFT_CURLY, "Expect '{' after PUNDOK statement. Code block: " + blockName, false);
        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after '{' in PUNDOK statement.", false);

        while (!isCurrTokenType(TokenType.RIGHT_CURLY) && !isAtEnd()){
            parseStatements(blockStatements);
            consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after a statement inside PUNDOK", false);
        }

        consumeToken(TokenType.RIGHT_CURLY, "Expected '}' after block. Code block: " + blockName, false);
        consumeToken(TokenType.NEW_LINE, "Expected NEW_LINE after PUNDOK for " + blockName + " block.", true);
        return blockStatements;
    }

    // PARSING EXPRESSION STATEMENTS - refers mostly to mathematical expressions
    private Stmt parseExprStatement() throws ParserException {
        Expr expr = parseExpression();
        if (!(expr instanceof Expr.Assign)){
            throw new ParserException("Invalid statement.", getPrevToken().getLine());
        }
        return new Stmt.Expression(expr);
    }

    // expression - equality - comparison - term - factor - unary - primary
    private Expr parseExpression() throws ParserException {
        return parseAssignment();
    }

    // check assigning variable value
    private Expr parseAssignment() throws ParserException {
        Expr expr = parseLogicalOR();

        if (matchToken(TokenType.EQUAL)){
            System.out.println("Assignment");
            Token equals = getPrevToken();
            Expr value = parseAssignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable) expr).name;
                System.out.println(name);
                astPrinter.print(value);
                return new Expr.Assign(name, value);
            }

            if (expr instanceof Expr.Literal){
                throw new ParserException("Invalid assignment target. Cannot assign to a Literal. Maybe you meant '=='?", getPrevToken().getLine());
            }

            throw new ParserException("Invalid assignment target.", equals.getLine());
        }


        return expr;
    }

    private Expr parseLogicalOR() throws ParserException {
        Expr expr = parseLogicalAND();

        while (matchToken(TokenType.LOGIC_OR)){
            Token operator = getPrevToken();
            Expr right = parseLogicalAND();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseLogicalAND() throws ParserException {
        Expr expr = parseEquality();

        while (matchToken(TokenType.LOGIC_AND)){
            Token operator = getPrevToken();
            Expr right = parseEquality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseEquality() throws ParserException {
        Expr expr = parseComparison();

        while (matchToken(TokenType.NOT_EQUAL, TokenType.DOUBLE_EQUAL)) {
            Token operator = getPrevToken();
            Expr right = parseComparison();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseComparison() throws ParserException {
        Expr expr = parseTerm();

        while (matchToken(TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.LESSER_THAN, TokenType.LESSER_OR_EQUAL)) {
            Token operator = getPrevToken();
            Expr right = parseTerm();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseTerm() throws ParserException {
        Expr expr = parseFactor();

        while (matchToken(TokenType.MINUS, TokenType.PLUS, TokenType.CONCAT)) {
            Token operator = getPrevToken();
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseFactor() throws ParserException {
        Expr expr = parseUnary();

        while (matchToken(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO)) {
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

            if (operator.getTokenType() == TokenType.LOGIC_NOT){
                return new Expr.Logical(null, operator, right);
            }

            return new Expr.Unary(operator, right);
        }

        return parsePrimary();
    }

    private Expr parsePrimary() throws ParserException {
        if (isAtEnd()){
            throw new ParserException("Unexpected EOF while parsing. Expected expression not found.", getPrevToken().getLine());
        }

        System.out.println("parsePrimary: " + getCurrToken().getLiteral() + getCurrToken().getTokenType());
        if (matchToken(TokenType.BOOL_FALSE, TokenType.BOOL_TRUE)) return new Expr.Literal("boolean", getPrevToken().getLiteral());
        if (matchToken(TokenType.NULL)) return new Expr.Literal("null", null);
        if (matchToken(TokenType.CHARACTER)) return new Expr.Literal("character", getPrevToken().getLiteral());
        if (matchToken(TokenType.INTEGER)) return new Expr.Literal("integer", getPrevToken().getLiteral());
        if (matchToken(TokenType.DOUBLE)) return new Expr.Literal("double", getPrevToken().getLiteral());
        if (matchToken(TokenType.STRING)) return new Expr.Literal("string", getPrevToken().getLiteral());
        if (matchToken(TokenType.ESCAPE_CHAR)) {
            char esc = (char) getPrevToken().getLiteral();
            return switch (esc) {
                case 'r' -> new Expr.Literal("character", '\r');
                case 'n' -> new Expr.Literal("character", '\n');
                case 't' -> new Expr.Literal("character", '\t');
                default -> new Expr.Literal("character", getPrevToken().getLiteral());
            };
        }
        if (matchToken(TokenType.IDENTIFIER)) return new Expr.Variable(getPrevToken());
        if (matchToken(TokenType.LEFT_PAREN)){
            Expr expr = parseExpression();
            consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after expression.", false);
            System.out.println("return grouping");
            return new Expr.Grouping(expr);
        }
        if (matchToken(TokenType.CNEW_LINE)) return new Expr.Literal("character", '\n');
        if (matchToken(TokenType.NEW_LINE)) throw new ParserException("Expected new statement in line. 1 statement per line.", getPrevToken().getLine());

        if (matchToken(TokenType.IF_ELSE, TokenType.ELSE)){
            throw new ParserException("Invalid syntax for IF statement. Missing IF code block.", getPrevToken().getLine());
        }

        Token token = getCurrToken();
        throw new ParserException("Unexpected token '" +   ((token.getLiteral() == null ? token.getTokenType().toString() : token.getLiteral())) + "' found. Expected an expression.", token.getLine());
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
    private Token consumeToken(TokenType expectedType, String message, boolean isEndOfStmt) throws ParserException {
        if (isAtEnd() && !isEndOfStmt) {
            throw new ParserException("Unexpected EOF while parsing. " + message, getPrevToken().getLine());
        } else if (isAtEnd() && isEndOfStmt && expectedType == TokenType.NEW_LINE){
            throw new ParserException("Expected 'KATAPUSAN' at end of program.", getPrevToken().getLine() + 1);
        }

        if (isCurrTokenType(expectedType)) return advance(); // if the token type matches, it will increment current counter

        // if the type does not match
        Token token = getCurrToken();

        // throws exception. it gives the message and the token that was found instead of the expected tokentype
        throw new ParserException(message + " Received: " + ((token.getLiteral()) == null ? token.getTokenType().toString() : token.getLiteral()), token.getLine());
    }

    // checks if the current expr has any of those token types
    // automatically advances to the next token type if found a match
    // accepts one or many tokentypes to check
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
        return current >= tokens.size();
    }
}

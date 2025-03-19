package com.example.bisayaplusplus.lexer;

import com.example.bisayaplusplus.exception.IllegalCharacterException;
import com.example.bisayaplusplus.exception.LexerException;
import com.example.bisayaplusplus.exception.UnexpectedTokenException;
import com.example.bisayaplusplus.exception.UnterminatedStringException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String program;
    private final List<Token> tokens = new ArrayList<>();
    private int start, current = 0, line = 1;

    public static final Map<String, TokenType> keywords = new HashMap<>(){
        {
            put("SUGOD", TokenType.START_STMT);
            put("KATAPUSAN", TokenType.END_STMT);

            put("MUGNA", TokenType.CREATE_STMT);
            put("IPAKITA", TokenType.PRINT_STMT);
            put("DAWAT", TokenType.READ_STMT);

            put("NUMERO", TokenType.INT_TYPE);
            put("LETRA", TokenType.CHAR_TYPE);
            put("TIPIK", TokenType.DOUBLE_TYPE);
            put("TINUOD", TokenType.BOOL_TYPE);

            put("KUNG", TokenType.IF);
            put("DILI", TokenType.IF_ELSE);
            put("WALA", TokenType.ELSE);
            put("PUNDOK", TokenType.CODE_BLOCK);
            put("ALANG SA", TokenType.FOR_LOOP);
        }
    };

    public Lexer(String program){
        this.program = program;
    }

    public List<Token> scanTokens() throws IllegalCharacterException, UnexpectedTokenException, UnterminatedStringException, LexerException {
        while (!isAtEnd()){
            start = current;
            scanToken();
        }

        return tokens;
    }

    private void scanToken() throws LexerException, IllegalCharacterException, UnterminatedStringException, UnexpectedTokenException {
        char c = getCurrCharThenNext();

        switch(c){
            case '{': addToken(TokenType.LEFT_CURLY); break;
            case '}': addToken(TokenType.RIGHT_CURLY); break;
            // ARITHMETIC OPERATORS
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '#': addToken(TokenType.HASHTAG); break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/': addToken(TokenType.DIVIDE); break;
            case '%': addToken(TokenType.MODULO);break;
            case '+': addToken(isUnaryToken() ? TokenType.POSITIVE : TokenType.PLUS); break;
            case '-': // comment
                if (charMatch('-')){
                    while (getNextChar() != '\n' && !isAtEnd()) getCurrCharThenNext();
                    getCurrCharThenNext(); // flush out newline
                } else if (isUnaryToken()) {
                    addToken(TokenType.NEGATIVE); // unary operator
                } else {
                    addToken(TokenType.MINUS); // binary operator
                }
                break;
            case '=': addToken(charMatch('=') ? TokenType.DOUBLE_EQUAL : TokenType.EQUAL); break;
            case '>': addToken(charMatch('=') ? TokenType.GREATER_OR_EQUAL : TokenType.GREATER_THAN); break;
            case '<': addToken(charMatch('=') ? TokenType.LESSER_OR_EQUAL : charMatch('>') ? TokenType.NOT_EQUAL : TokenType.LESSER_THAN);break;

            // PROGRAM - SPECIAL CHAR
            case '$': addToken(TokenType.CNEW_LINE);break;
            case '&': addToken(TokenType.CONCAT); break;
            case '[': // open escape code
                addToken(TokenType.ESCAPE_CHAR, getCurrCharThenNext());
                if (!charMatch(']')){
                    throw new UnexpectedTokenException(getCurrCharThenNext() + "", "Expected closing escape code ']'.", line);
                }
                getCurrCharThenNext(); // flush closing escape code
            // for whitespaces
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n':
                addToken(TokenType.NEW_LINE);
                line++;
                break;
            case '\"': // string literal
                addTokenString();
                break;
            case '\'': // character literal
                addTokenChar();
                break;
            case '_': // starting identifier
                addTokenIdentifier();
                break;
            case ':': addToken(TokenType.COLON);break;
            case ',': addToken(TokenType.COMMA); break;
            default:
                if (Character.isDigit(c)){ // number literal
                    addTokenNumber();
                } else if (Character.isLetter(c)) { // identifier
                    addTokenIdentifier();
                } else {
                    throw new IllegalCharacterException(c + " ", line);
                }
        }
    }

    // function to get next character & increment the current counter
    private char getCurrCharThenNext(){
        return program.charAt(current++);
    }

    // get next character without incrementing the current counter
    private char getNextChar(){
        if (isAtEnd()) return '\0';
        return program.charAt(current);
    }

    // function to take in variable names
    private void addTokenIdentifier(){
        while (isIdentifierChar(getNextChar())){
            getCurrCharThenNext();
        }

        String value = program.substring(start, current);
        TokenType type = keywords.get(value.toUpperCase());

        if (type == null){
            addToken(TokenType.IDENTIFIER, value);
        }
        else {
            addToken(type);
        }
    }

    // function to check if the char kay valid siya sa identifier
    private boolean isIdentifierChar(char c){
        if (Character.isLetter(c) || Character.isDigit(c) || c == '_'){
            return true;
        }

        return false;
    }

    // function to get literal string
    private void addTokenString() throws UnterminatedStringException {
        while (getNextChar() != '\"' && !isAtEnd()){
            if (getNextChar() == '\n') line++;
            getCurrCharThenNext();
        }

        if (isAtEnd()){
            // error
            throw new UnterminatedStringException(" addtokenstring", line);
        }

        getCurrCharThenNext(); // closing "

        String value = program.substring(start + 1, current-1);

        if (value.equals("OO")){
            addToken(TokenType.BOOL_TRUE, "OO");
        } else if (value.equals("DILI")) {
            addToken(TokenType.BOOL_FALSE, "DILI");
        } else {
            addToken(TokenType.STRING, value);
        }
    }

    // function get literal number
    private void addTokenNumber() throws UnexpectedTokenException, LexerException {
        while (!isAtEnd() && Character.isDigit(getNextChar())){
            System.out.println(getCurrCharThenNext());
        }

        if (charMatch(' ') || charMatch(',') || charMatch('\n') || isAtEnd()){
            System.out.println("number: "+  program.substring(start, current));
            addToken(TokenType.INTEGER, Integer.parseInt(program.substring(start, current-1)));

            if (!isAtEnd()){
                current--;
            }
        } else if (getNextChar() == '.'){
            getCurrCharThenNext();

            while (!isAtEnd() && Character.isDigit(getNextChar())){
                getCurrCharThenNext();
            }

            if (!isAtEnd() && !charMatch(' ') && !charMatch(',') && !charMatch('\n')){
                throw new UnexpectedTokenException(program.charAt(current) + "", "Expected a number for the fractional part.", line);
            }

            addToken(TokenType.DOUBLE, Double.parseDouble(program.substring(start, current)));
            System.out.println("DOUBLE" + program.substring(start, current));
            current--;
        } else {
            throw new LexerException("Unexpected identifier-like sequence after a number (" + program.substring(start, current) + ").", line);
        }
    }

    // function to add character
    private void addTokenChar() throws UnexpectedTokenException {
        addToken(TokenType.CHARACTER, getCurrCharThenNext());

        if (getNextChar() != '\''){
            throw new UnexpectedTokenException(getNextChar() + "", "Expected closing ' for character literals.", line);
        }

        getCurrCharThenNext();
    }

    // check if the current character matched the expected character
    private boolean charMatch(char expected){
        if (isAtEnd()) return false;
        if (program.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // function to check if the lexer is already at the end of the program ode
    private boolean isAtEnd(){
        return current >= program.length();
    }

    // add new token to list
    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        tokens.add(new Token(type, literal, line));
    }

    // function to check if the previous token is a number
    // if number -> return false ; if not -> return true
    private boolean isUnaryToken(){
        return (checkPrevToken() == TokenType.INTEGER || checkPrevToken() == TokenType.DOUBLE);
    }
    // function to check the recently added token type
    private TokenType checkPrevToken(){
        return tokens.get(tokens.size()-1).getTokenType();
    }
}

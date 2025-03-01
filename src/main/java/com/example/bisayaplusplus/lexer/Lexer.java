package com.example.bisayaplusplus.lexer;

import com.example.bisayaplusplus.exception.IllegalCharacterException;
import com.example.bisayaplusplus.exception.UnexpectedTokenException;
import com.example.bisayaplusplus.exception.UnterminatedStringException;

import java.io.CharConversionException;
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
            put("TIPIK", TokenType.FLOAT_TYPE);
            put("TINUOD", TokenType.BOOL_TYPE);
        }
    };

    public Lexer(String program){
        this.program = program;
    }

    public List<Token> scanTokens() throws IllegalCharacterException, UnexpectedTokenException, UnterminatedStringException {
        while (!isAtEnd()){
            start = current;
            scanToken();
        }

        return tokens;
    }

    private void scanToken() throws IllegalCharacterException, UnterminatedStringException, UnexpectedTokenException {
        char c = advance();

        switch(c){
            case '[': addTokenCode(); break;
            case '#': addToken(TokenType.HASHTAG); break;
            case '&': addToken(TokenType.AMPERSAND); break;
            case '=': addToken(match('=') ? TokenType.DOUBLE_EQUAL : TokenType.EQUAL); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '-': // comment
                if (match('-')){
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '+': addToken(TokenType.PLUS); break;
            case '/': addToken(TokenType.DIVIDE); break;
            case '%': addToken(TokenType.MODULO);break;
            case '>': addToken(match('=') ? TokenType.GREATER_OR_EQUAL : TokenType.GREATER_THAN); break;
            case '<': addToken(match('=') ? TokenType.LESSER_OR_EQUAL : match('>') ? TokenType.NOT_EQUAL : TokenType.LESSER_THAN);break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n':
                addToken(TokenType.NEW_LINE);
                line++;
                break;
            case '\"':
                // string
                addTokenString();
                break;
            case '\'':
                addTokenChar();
                break;
            case '_':
                addTokenIdentifier();
                break;
            case ',': addToken(TokenType.COMMA);break;
            case ':': addToken(TokenType.COLON);break;
            case '$': addToken(TokenType.DOLLAR_SIGN);break;
            default:
                if (Character.isDigit(c)){
                    addTokenNumber();
                } else if (Character.isLetter(c)) {
                    addTokenIdentifier();
                } else {
                    throw new IllegalCharacterException(c + " ", line);
                }
        }
    }

    // see next character
    private char advance(){
        return program.charAt(current++);
    }

    // see next character without incrementing the current counter
    private char peek(){
        if (isAtEnd()) return '\0';
        return program.charAt(current);
    }

    private void addTokenCode(){
        while (peek() != ']'){
            advance();
        }

        addToken(TokenType.STRING, program.substring(start, current));
        advance(); // flush ]
    }

    private void addTokenIdentifier(){
        while (checkIdentifierChar(peek())){
            advance();
        }

        String value = program.substring(start, current);
        TokenType type = keywords.get(value);

        if (type == null){
            addToken(TokenType.IDENTIFIER, value);
        } else {
            addToken(type);
        }
    }

    private boolean checkIdentifierChar(char c){
        if (Character.isLetter(c) || Character.isDigit(c) || c == '_'){
            return true;
        }

        return false;
    }

    // function to get literal string
    private void addTokenString() throws UnterminatedStringException {
        while (peek() != '\"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()){
            // error
            throw new UnterminatedStringException(" addtokenstring", line);
        }

        advance(); // closing "

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
    private void addTokenNumber() throws UnexpectedTokenException {
        boolean hasPeriod = false;
        while (Character.isDigit(peek())){
            advance();
        }

        if (peek() == '.'){
            hasPeriod = true;
            advance();

            if (!Character.isDigit(current)){
                throw new UnexpectedTokenException(program.charAt(current)+" addotkennumeber1", line);
            }

            while (Character.isDigit(peek())){
                advance();
            }

            if (!Character.isDigit(current)){
                throw new UnexpectedTokenException(program.charAt(current)+" addtokenenumber2", line);
            }

            addToken(TokenType.FLOAT, Double.parseDouble(program.substring(start, current)));
        } else {
            addToken(TokenType.INTEGER, Integer.parseInt(program.substring(start, current)));
        }
    }

    private void addTokenChar() throws UnexpectedTokenException {
        if (peek() == '\'' || isAtEnd()){
            throw new UnexpectedTokenException("\'", line);
        }

        addToken(TokenType.CHARACTER, advance());

        if (peek() != '\''){
            throw new UnexpectedTokenException(peek() + " addtokenchar", line);
        }

        advance();
    }

    private boolean match(char expected){
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


}

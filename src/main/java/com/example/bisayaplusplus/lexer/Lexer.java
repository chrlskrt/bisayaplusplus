/* LEXER
 * This class is responsible for scanning and converting the source code into tokens.
 * It breaks down the code into its fundamental building blocks such as
 * keywords, operators, literals, and identifiers.
 *
 * It handles whitespace, comments, and tracks line/position information for error reporting.
 * The token stream it produces serves as input for the parser in the next compilation stage.
 *
 * If the lexer will encounter an error, it will stop scanning and reflect the error to the user.
 */

package com.example.bisayaplusplus.lexer;

import com.example.bisayaplusplus.exception.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    // Source code
    private final String program;

    // List of tokens
    private final List<Token> tokens = new ArrayList<>();

    // Lexer variables
    private int start, current = 0, line = 1;

    // Reserved words for Bisaya++
    public static final Map<String, TokenType> keywords = new HashMap<>(){
        {
            // START and END keywords
            put("SUGOD", TokenType.START_STMT);
            put("KATAPUSAN", TokenType.END_STMT);

            // DECLARATION keywords
            put("MUGNA", TokenType.CREATE_STMT);
            put("IPAKITA", TokenType.PRINT_STMT);
            put("DAWAT", TokenType.READ_STMT);

            // DATA_TYPE keywords
            put("NUMERO", TokenType.INT_KEYWORD);
            put("LETRA", TokenType.CHAR_KEYWORD);
            put("TIPIK", TokenType.DOUBLE_KEYWORD);
            put("TINUOD", TokenType.BOOL_KEYWORD);

            // IF statement
            put("KUNG", TokenType.IF);
            put("KUNG DILI", TokenType.IF_ELSE);
            put("KUNG WALA", TokenType.ELSE);

            // Block keyword
            put("PUNDOK", TokenType.CODE_BLOCK);

            // FOR loop
            put("ALANG SA", TokenType.FOR_LOOP);

            // WHILE LOOP
            put("MINTRAS", TokenType.WHILE_LOOP);

            // DO-WHILE LOOP
            put("BUHATA", TokenType.DO_WHILE_LOOP);

            // null
            put("null", TokenType.NULL);

            // LOGICAL operators
            put("O", TokenType.LOGIC_OR);
            put("DILI", TokenType.LOGIC_NOT);
            put("UG", TokenType.LOGIC_AND);
        }
    };

    // Constructor
    public Lexer(String program){
        this.program = program;
    }

    /*
    * The function that will scan the source code
    * and convert to tokens.
    */
    public List<Token> scanTokens() throws LexerException {
        while (!isAtEnd()){
            start = current;
            scanToken();
        }

        return tokens;
    }

    // Scanning individual character to convert into a token
    private void scanToken() throws LexerException {
        char c = getCurrCharThenNext();

        switch(c){
            case '{': addToken(TokenType.LEFT_CURLY); break;
            case '}': addToken(TokenType.RIGHT_CURLY); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case ':': addToken(TokenType.COLON);break;
            case ',': addToken(TokenType.COMMA); break;

            // ARITHMETIC OPERATORS w special cases
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/': addToken(TokenType.DIVIDE); break;
            case '%': addToken(TokenType.MODULO);break;
            case '+':
                if (charMatch('+')){
                    addToken(TokenType.INCREMENT);
                } else if (isUnaryToken()){
                    addToken(TokenType.POSITIVE);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;
            case '-': 
                // comment
                if (charMatch('-')){
                    while (!isAtEnd() && getNextChar() != '\n'){
                        getCurrCharThenNext();
                    }
                } else if (isUnaryToken()) {
                    addToken(TokenType.NEGATIVE); // unary operator
                } else {
                    addToken(TokenType.MINUS); // binary operator
                }
                break;

            // COMPARATORS
            case '=': addToken(charMatch('=') ? TokenType.DOUBLE_EQUAL : TokenType.EQUAL); break;
            case '>': addToken(charMatch('=') ? TokenType.GREATER_OR_EQUAL : TokenType.GREATER_THAN); break;
            case '<':
                if (charMatch('=')){
                    addToken(TokenType.LESSER_OR_EQUAL);
                } else if (charMatch('>')){
                    addToken(TokenType.NOT_EQUAL);
                } else {
                    addToken(TokenType.LESSER_THAN);
                }
                break;

            // PROGRAM - SPECIAL CHAR
            case '$': addToken(TokenType.CNEW_LINE);break;
            case '&': addToken(TokenType.CONCAT); break;
            case '[': // open escape code
                if (isAtEnd()){
                    throw new UnexpectedEOF("Missing escape character and closing escape code.", line);
                }

                // storing the escape char as ESCAPE_CHAR token
                addToken(TokenType.ESCAPE_CHAR, getCurrCharThenNext());

                if (isAtEnd()){
                    throw new UnexpectedEOF("Missing closing escape code.", line);
                }

                if (!charMatch(']')){
                    throw new UnexpectedTokenException(getCurrCharThenNext() + "", "Only 1 character need for escape. Expected closing escape code ']'.", line);
                }

            // for whitespaces
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;

            case '\n':
                if (checkPrevToken() != null && checkPrevToken() != TokenType.NEW_LINE){
                    addToken(TokenType.NEW_LINE);
                }
                line++;
                break;

            // LITERALS
            case '\"': // string literal
                addTokenString();
                break;
            case '\'': // character literal
                addTokenChar();
                break;
            case '_': // starting identifier
                addTokenIdentifier();
                break;
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

    // function to take in variable names
    private void addTokenIdentifier(){
        // traverse through the string while it deems the 
        // current character to be legible as an identifier name 
        while (!isAtEnd() && isIdentifierChar(getNextChar())){
            getCurrCharThenNext();
        }

        // getting string
        String value = program.substring(start, current);
        
        // checks if string is a keyword
        TokenType type = keywords.get(value);

        // conditional - if-else
        // control struc - for loop
        if ((type == TokenType.IF || value.equals("ALANG")) && charMatch(' ')){
            int tempCurr = current-1;
            while (isIdentifierChar(getNextChar())){
                getCurrCharThenNext();
            }

            String newVal = program.substring(start, current);
            TokenType newType = keywords.get(newVal);

            if (newType == null){
                current = tempCurr;
            } else {
                type = newType;
            }
        } else if (type == null) {
            addToken(TokenType.IDENTIFIER, value);
            return;
        }

        addToken(type);
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

    // function to get literal number
    private void addTokenNumber() throws LexerException {
        while (!isAtEnd() && Character.isDigit(getNextChar())){
            getCurrCharThenNext();
        }

        if (!isAtEnd() && isIdentifierChar(program.charAt(current))){
            throw new LexerException("Unexpected identifier-like sequence after a number (" + program.substring(start, current) + ").", line);
        } else if (getNextChar() == '.'){
            // decimal number
            getCurrCharThenNext();

            // getting fractional part
            while (!isAtEnd() && Character.isDigit(getNextChar())){
                getCurrCharThenNext();
            }

            if (isIdentifierChar(program.charAt(current))){
                throw new UnexpectedTokenException(program.charAt(current) + "", "Expected a number for the fractional part.", line);
            }

            addToken(TokenType.DOUBLE, Double.parseDouble(program.substring(start, current)));
        } else {
            addToken(TokenType.INTEGER, Integer.parseInt(program.substring(start, current)));
        }
    }

    // function to add character literal
    private void addTokenChar() throws UnexpectedTokenException, UnexpectedEOF {
        if (isAtEnd()){
            throw new UnexpectedEOF("Missing character literal.", line);
        }

        addToken(TokenType.CHARACTER, getCurrCharThenNext());

        if (getNextChar() != '\''){
            throw new UnexpectedTokenException(getNextChar() + "", "Expected closing ' for character literals.", line);
        }

        getCurrCharThenNext();
    }

    //-------------- UTIL FUNCTIONS ------------------

    // function to get next character & increment the current counter
    private char getCurrCharThenNext(){
        return program.charAt(current++);
    }

    // get next character without incrementing the current counter
    private char getNextChar(){
        if (isAtEnd()) return '\0';
        return program.charAt(current);
    }

    // checks if the current character matched the expected character
    // if matched, current counter is incremented by 1
    private boolean charMatch(char ...expected){
        if (isAtEnd()) return false;

        for (char exp: expected){
            if (program.charAt(current) == exp){
                current++;
                return true;
            }
        }

        return false;
    }

    // function to check if the lexer is already at the end of the program ode
    private boolean isAtEnd(){
        return current >= program.length();
    }

    // function to check if the char kay valid siya sa identifier
    private boolean isIdentifierChar(char c){
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    // function to check if the previous token is a number
    // if number -> return false ; if not -> return true
    private boolean isUnaryToken(){
        System.out.println("checkUnary: " + checkPrevToken());
        return !(checkPrevToken() == TokenType.INTEGER || checkPrevToken() == TokenType.DOUBLE || checkPrevToken() == TokenType.IDENTIFIER);
    }
    // function to check the recently added token type
    private TokenType checkPrevToken(){
        if (tokens.isEmpty()){
            return null;
        }

        return tokens.get(tokens.size()-1).getTokenType();
    }

    // add new token to list
    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        tokens.add(new Token(type, literal, line));
    }
}

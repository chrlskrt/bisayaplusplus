package com.example.bisayaplusplus.exception;

public class LexerException extends Exception {
    String character;
    int line;

    public LexerException (String character, int line){
        this.character = character;
        this.line = line;
    }
    @Override
    public String getMessage() {
        return "Lexer exception: ";
    }
}

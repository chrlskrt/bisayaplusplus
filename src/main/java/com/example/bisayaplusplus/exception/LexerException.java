package com.example.bisayaplusplus.exception;

import com.example.bisayaplusplus.lexer.Lexer;

public class LexerException extends Exception {
    String message;
    int line;

    public LexerException (String message, int line){
        this.message = message;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return "Lexer exception: " + line + " : " + message + " ";
    }
}

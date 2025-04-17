package com.example.bisayaplusplus.exception;

import com.example.bisayaplusplus.lexer.Token;

public class RuntimeError extends RuntimeException{
    final Token token;

    public RuntimeError(Token token, String message){
        super(message);
        this.token = token;
    }

    @Override
    public String getMessage() {
        return "[line " + token.getLine() + "] Runtime Error: " + super.getMessage();
    }
}

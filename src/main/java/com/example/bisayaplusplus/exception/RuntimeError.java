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
        return "Runtime Error: " + super.getMessage() + " at line " + token.getLine();
    }
}

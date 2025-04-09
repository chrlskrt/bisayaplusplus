package com.example.bisayaplusplus.exception;

import com.example.bisayaplusplus.lexer.Token;

public class ParserException extends Exception{
    public String errMessage;
    public int line;
    public ParserException(String errMessage, int line){
        this.errMessage = errMessage;
        this.line = line;
    }
    @Override
    public String getMessage() {
        return "[line " + line + "] Parser error: " + errMessage;
    }
}

package com.example.bisayaplusplus.exception;

public class ParserException extends Exception{
    public String tokenValue;
    public int line;
    public ParserException(String tokenValue, int line){
        this.tokenValue = tokenValue;
        this.line = line;
    }
    @Override
    public String getMessage() {
        return "Parser error: " + tokenValue + " at line " + line;
    }
}

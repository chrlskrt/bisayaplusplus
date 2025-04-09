package com.example.bisayaplusplus.exception;

public class UnexpectedEOF extends LexerException{
    public UnexpectedEOF(String message, int line) {
        super(message, line);
    }
}

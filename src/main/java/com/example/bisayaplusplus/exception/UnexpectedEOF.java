package com.example.bisayaplusplus.exception;

public class UnexpectedEOF extends LexerException{
    public UnexpectedEOF(String message, int line) {
        super(message, line);
    }

    @Override
    public String getMessage() {
       return "[line " + line + "] Lexer exception: Unexpected EOF while parsing. " + super.message;
    }
}

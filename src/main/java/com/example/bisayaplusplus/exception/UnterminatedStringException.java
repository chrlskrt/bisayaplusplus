package com.example.bisayaplusplus.exception;

public class UnterminatedStringException extends LexerException{

    public UnterminatedStringException(String character, int line) {
        super(character, line);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "Unterminated string at line " + line + ".";
    }
}

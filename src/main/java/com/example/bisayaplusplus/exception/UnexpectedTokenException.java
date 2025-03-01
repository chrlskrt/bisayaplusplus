package com.example.bisayaplusplus.exception;

public class UnexpectedTokenException extends LexerException {
    public UnexpectedTokenException(String character, int line) {
        super(character, line);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "Unexpected token \"" + character + "\" at line " + line + ".";
    }
}

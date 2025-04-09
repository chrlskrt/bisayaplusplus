package com.example.bisayaplusplus.exception;

public class UnexpectedTokenException extends LexerException {
    String message;
    public UnexpectedTokenException(String character, String message, int line) {
        super(character, line);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[line " + line + "] Lexer exception: Unexpected token \"" + super.message + "\". " + message;
    }
}

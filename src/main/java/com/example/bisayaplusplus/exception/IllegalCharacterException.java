package com.example.bisayaplusplus.exception;

public class IllegalCharacterException extends LexerException {
    public IllegalCharacterException(String message, int line) {
        super(message, line);
    }

    @Override
    public String getMessage() {
        return "[line " + line + "] Lexer exception: Illegal character \"" + message + "\".";
    }
}


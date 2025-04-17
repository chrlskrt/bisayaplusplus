package com.example.bisayaplusplus.exception;

public class InvalidNumberException extends LexerException{

    public InvalidNumberException(String character, int line) {
        super(character, line);
    }

    @Override
    public String getMessage() {
        return "[line " + line + "] Lexer exception:  Invalid number \"" + super.message + "\".";
    }
}

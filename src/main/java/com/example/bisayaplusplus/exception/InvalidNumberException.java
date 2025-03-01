package com.example.bisayaplusplus.exception;

public class InvalidNumberException extends LexerException{

    public InvalidNumberException(String character, int line) {
        super(character, line);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "Invalid number \"" + character + "\" at line " + line + ".";
    }
}

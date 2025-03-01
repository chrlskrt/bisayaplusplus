package com.example.bisayaplusplus.exception;

public class IllegalCharacterException extends LexerException {
    public IllegalCharacterException(String character, int line) {
        super(character, line);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "Illegal character \"" + character + "\" at line " + line + ".";
    }
}


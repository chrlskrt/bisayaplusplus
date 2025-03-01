package com.example.bisayaplusplus.lexer;

public class Token {
    final TokenType type;
    final Object literal;
    final int line;

    public Token(TokenType type, Object literal, int line) {
        this.type = type;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return literal + ": " + type + " @line " + line;
    }

    public Object getLiteral(){
        return literal;
    }

    public TokenType getTokenType(){
        return type;
    }

    public int getLine(){
        return line;
    }
}

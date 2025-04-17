package com.example.bisayaplusplus.lexer;

class Token {
    final TokenType type;
    final String lexeme; // substrings
    final Object literal; // value
    final int line; // for error msg

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + "substring: " + lexeme + "value: " + literal;
    }
}

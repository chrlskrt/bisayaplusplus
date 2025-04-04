package com.example.bisayaplusplus.lexer;

public enum TokenType {
    // SINGLE CHARACTER
    LEFT_CURLY, RIGHT_CURLY, LEFT_PAREN, RIGHT_PAREN, UNDERSCORE,
    COLON, HASHTAG, COMMA,
    // operators
    NEGATIVE, POSITIVE,
    PLUS, MINUS, MULTIPLY, DIVIDE, EQUAL,
    GREATER_THAN, LESSER_THAN, MODULO,

    // DOUBLE CHARACTER
    DOUBLE_EQUAL, NOT_EQUAL, GREATER_OR_EQUAL, LESSER_OR_EQUAL,
    DOUBLE_MINUS,

    // LITERALS
    IDENTIFIER, STRING, INTEGER, DOUBLE,
    BOOL_TRUE, BOOL_FALSE, CHARACTER,
    NULL, ESCAPE_CHAR,

    // logical operators
    LOGIC_OR, LOGIC_NOT, LOGIC_AND,

    // RESERVED WORDS
    // data type
    INT_KEYWORD, DOUBLE_KEYWORD, CHAR_KEYWORD, BOOL_KEYWORD,

    // keywords
    START_STMT, END_STMT, CREATE_STMT, READ_STMT, PRINT_STMT,

    // control structures
    IF, IF_ELSE, ELSE, FOR_LOOP, CODE_BLOCK,

    // special character
    NEW_LINE, CONCAT, CNEW_LINE,
}

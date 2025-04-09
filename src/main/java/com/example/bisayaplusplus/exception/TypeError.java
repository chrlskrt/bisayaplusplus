package com.example.bisayaplusplus.exception;

import com.example.bisayaplusplus.lexer.Token;

public class TypeError extends RuntimeException{
    final Token token;
    final String valueDataType;
    final String varName;
    final String varDataType;
    public TypeError(Token token, String valueDataType, String varName, String varDataType) {
        this.token = token;
        this.valueDataType = valueDataType;
        this.varName = varName;
        this.varDataType = varDataType;
    }

    @Override
    public String getMessage() {
        return "[line " + token.getLine() + "] TypeError: Cannot assign value of type '" + valueDataType + "'" +
        " to the variable '" + varName + "' of type '" + varDataType + "'";
    }
}

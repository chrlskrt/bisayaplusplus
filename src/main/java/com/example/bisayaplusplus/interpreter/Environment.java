/* ENVIRONMENT
 * This class manages the environment for the interpreter, handling variable scope
 * and storage. It utilizes a SymbolTable internally to maintain mappings between
 * variable names, their data types, and their corresponding values.
 *
 * It supports nested scopes through an 'enclosing' environment and provides
 * functionalities for defining, retrieving, and assigning variable values and types.
 * Special handling is included for assigning values to variables based on input,
 * performing type conversions and checks as needed.
 */

package com.example.bisayaplusplus.interpreter;

import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.lexer.Token;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    SymbolTable symbolTable;
    public Environment(){
        enclosing = null;
        symbolTable = new SymbolTable(null);
    }

    public Environment(Environment enclosing, SymbolTable symbolTable){
        this.enclosing = enclosing;
        this.symbolTable = symbolTable;
    }

    public void define(Token var, String dataType, Object value){
        symbolTable.define(var, dataType, value);
    }

    public Object get(Token name){
        return symbolTable.get(name);
    }

    public String getType(Token name){
        return symbolTable.getType(name);
    }

    public void assign(Token name, Object value) {
        symbolTable.assign(name, value);
    }

    public void assignFromPrint(Token var, String value){
        String varDataType = symbolTable.getType(var);
        Object adjustedValue = null;
        value = value.trim();

        try {
            switch (varDataType){
                case "Integer":
                    adjustedValue = Integer.parseInt(value);
                    break;
                case "Double":
                    adjustedValue = Double.parseDouble(value);
                    break;
                case "Boolean":
                    if (value.equals("\"OO\"")){
                        adjustedValue = "OO";
                    } else if (value.equals("DILI")){
                        adjustedValue = "DILI";
                    } else {
                        throw new RuntimeError(var,"Incompatible input for variable " + var.getLiteral() + " with type Boolean.");
                    }
                    break;
                case "Character":
                    if (value.length() == 1){
                        adjustedValue = value.charAt(0);
                    };
                    break;
                default:
                    throw new RuntimeError(var, "Incompatible input for variable " + var.getLiteral() + " with type " + varDataType + ".");
            }
        } catch (NumberFormatException n){
            throw new RuntimeError(var, "Expect " + varDataType + " but received " + value);
        }

        symbolTable.assign(var, adjustedValue);
    }
    
    public void print(){
        symbolTable.print();
    }
}

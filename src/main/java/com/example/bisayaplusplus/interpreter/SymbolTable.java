/* SYMBOL TABLE
 * This class manages the storage and retrieval of variables and their associated
 * data types within a specific scope. It uses two HashMaps to store the variable
 * name along with its value and its data type, respectively.
 *
 * It supports nested scopes through an 'enclosing' SymbolTable, allowing for
 * variable shadowing and access to variables in outer scopes. Operations include
 * defining new variables, checking for redeclarations, retrieving variable values
 * and types, and assigning new values to existing variables.
 */

package com.example.bisayaplusplus.interpreter;

import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> dataTypes = new HashMap<>();
    private final SymbolTable enclosing;

    public SymbolTable(SymbolTable enclosing) {
        this.enclosing = enclosing;
    }

    public void define(Token var, String dataType, Object value){
        isDeclaredInAnyEnv(var);

        dataTypes.put(var.getLiteral().toString(), dataType);
        values.put(var.getLiteral().toString(), value);
    }

    public boolean isDeclaredInAnyEnv(Token var){
        if (dataTypes.containsKey(var.getLiteral().toString())){
            throw new RuntimeError(var, "Redeclaration of " + var.getLiteral());
        }

        // for strict non-redeclaration of variables in any scope.
//        if (enclosing != null){
//            return enclosing.isDeclaredInAnyEnv(var);
//        }

        return false;
    }

    public Object get(Token name){
        if (values.containsKey(name.getLiteral())){
            if (values.get(name.getLiteral()) == null){
                throw new RuntimeError(name, "Variable " + name.getLiteral() + " might not have been initialized.");
            }
            return values.get(name.getLiteral());
        };

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.getLiteral() + "'.");
    }

    public String getType(Token name){
        if (dataTypes.containsKey(name.getLiteral().toString())){
            return dataTypes.get(name.getLiteral().toString());
        };

        if (enclosing != null) return enclosing.getType(name);

        throw new RuntimeError(name, "Undefined variable '" + name.getLiteral() + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLiteral().toString())){
            values.put(name.getLiteral().toString(), value);
            return;
        }

        if (enclosing != null){
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.getLiteral().toString() + "'.");
    }

    public void print(){
        for (String key: values.keySet()){
            System.out.println(key + " = " + values.get(key));
        }
    }
}

package com.example.bisayaplusplus.interpreter;

import com.example.bisayaplusplus.exception.RuntimeError;
import com.example.bisayaplusplus.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    public Environment(){
        enclosing = null;
    }

    public Environment(Environment enclosing){
        this.enclosing = enclosing;
    }
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> dataTypes = new HashMap<>();

    public void define(Token var, String dataType, Object value){
        if (dataTypes.containsKey(var.getLiteral().toString())){
            throw new RuntimeError(var, "Redeclaration of " + var.getLiteral());
        }

        dataTypes.put(var.getLiteral().toString(), dataType);
        values.put(var.getLiteral().toString(), value);
    }

    public Object get(Token name){
        if (values.containsKey(name.getLiteral())){
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

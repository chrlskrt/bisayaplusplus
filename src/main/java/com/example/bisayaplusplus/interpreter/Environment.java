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

    public void assignFromPrint(Token var, String value){
        String varDataType = dataTypes.get(var.getLiteral().toString());
        Object adjustedValue = null;

        try {
            switch (varDataType){
                case "Integer":
                    adjustedValue = Integer.parseInt(value);
                    break;
                case "Double":
                    adjustedValue = Double.parseDouble(value);
                    break;
                case "Boolean":
                    if (value.equals("\"OO\"") || value.equals("OO") ){
                        adjustedValue = "OO";
                    } else if (value.equals("DILI") || value.equals("\"DILI\"")){
                        adjustedValue = "DILI";
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
            throw new RuntimeError(var, "Expect number but received " + value);
        }

        assign(var, adjustedValue);
    }
    
    public void print(){
        for (String key: values.keySet()){
            System.out.println(key + " = " + values.get(key));
        }
    }
}

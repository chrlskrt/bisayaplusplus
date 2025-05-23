package com.example.bisayaplusplus.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1){
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign    : Token name, Expr value",
                "Binary    : Expr left, Token operator, Expr right",
                "Grouping  : Expr expression",
                "Literal   : String dataType, Object value",
                "Logical   : Expr left, Token operator, Expr right",
                "Unary     : Token operator, Expr right",
                "Variable  : Token name",
                "IncrementOrDecrement : Token operator, Variable var, boolean isPrefix"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
           "Block      : List<Stmt> statements",
           "Expression : Expr expression",
           "If         : Expr condition, Stmt thenBranch, List<ElseIf> elseIfBranch, Stmt elseBranch",
           "ElseIf     : Expr condition, Stmt thenBranch",
           "Print      : Expr expression",
           // for loop that allows multiple initializations
//           "ForLoop    : List<Stmt> initialization, Expr condition, Expr update, Stmt body",
           "ForLoop    : Stmt initialization, Expr condition, Stmt update, Stmt body",
           "While      : Expr condition, Stmt body",
           "DoWhile    : Expr condition, Stmt body",
           "Var        : String dataType, Token name, Expr initializer",
           "Input      : List<Token> variables"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws FileNotFoundException, UnsupportedEncodingException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.example.bisayaplusplus.parser;");
        writer.println();
        writer.println("import com.example.bisayaplusplus.lexer.Token;\n");
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type: types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();

            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
        writer.println(" public static class " + className + " extends " + baseName + "{");
        // store parameters infields
        String[] fields = fieldList.split(", ");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        // constructor
        writer.println("    public " + className + " (" + fieldList + "){");

        for (String field: fields){
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        writer.println("  }");
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}

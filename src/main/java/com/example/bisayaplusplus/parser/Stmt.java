package com.example.bisayaplusplus.parser;

import com.example.bisayaplusplus.lexer.Token;

import java.util.List;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitExpressionStmt(Expression stmt);
    R visitIfStmt(If stmt);
    R visitElseIfStmt(ElseIf stmt);
    R visitPrintStmt(Print stmt);
    R visitForLoopStmt(ForLoop stmt);
    R visitWhileStmt(While stmt);
    R visitDoWhileStmt(DoWhile stmt);
    R visitVarStmt(Var stmt);
    R visitInputStmt(Input stmt);
  }
 public static class Block extends Stmt{

    public final List<Stmt> statements;
    public Block (List<Stmt> statements){
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
  }
 public static class Expression extends Stmt{

    public final Expr expression;
    public Expression (Expr expression){
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }
 public static class If extends Stmt{

    public final Expr condition;
    public final Stmt thenBranch;
    public final List<ElseIf> elseIfBranch;
    public final Stmt elseBranch;
    public If (Expr condition, Stmt thenBranch, List<ElseIf> elseIfBranch, Stmt elseBranch){
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseIfBranch = elseIfBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
  }
 public static class ElseIf extends Stmt{

    public final Expr condition;
    public final Stmt thenBranch;
    public ElseIf (Expr condition, Stmt thenBranch){
      this.condition = condition;
      this.thenBranch = thenBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitElseIfStmt(this);
    }
  }
 public static class Print extends Stmt{

    public final Expr expression;
    public Print (Expr expression){
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }
 public static class ForLoop extends Stmt{

    public final Stmt initialization;
    public final Expr condition;
    public final Stmt update;
    public final Stmt body;
    public ForLoop (Stmt initialization, Expr condition, Stmt update, Stmt body){
      this.initialization = initialization;
      this.condition = condition;
      this.update = update;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitForLoopStmt(this);
    }
  }
 public static class While extends Stmt{

    public final Expr condition;
    public final Stmt body;
    public While (Expr condition, Stmt body){
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
  }
 public static class DoWhile extends Stmt{

    public final Expr condition;
    public final Stmt body;
    public DoWhile (Expr condition, Stmt body){
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitDoWhileStmt(this);
    }
  }
 public static class Var extends Stmt{

    public final String dataType;
    public final Token name;
    public final Expr initializer;
    public Var (String dataType, Token name, Expr initializer){
      this.dataType = dataType;
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }
 public static class Input extends Stmt{

    public final List<Token> variables;
    public Input (List<Token> variables){
      this.variables = variables;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitInputStmt(this);
    }
  }

  public abstract <R> R accept(Visitor<R> visitor);
}

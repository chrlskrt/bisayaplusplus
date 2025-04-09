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
    R visitVarStmt(Var stmt);
  }
 public static class Block extends Stmt{
    public Block (List<Stmt> statements){
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    public final List<Stmt> statements;
  }
 public static class Expression extends Stmt{
    public Expression (Expr expression){
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    public final Expr expression;
  }
 public static class If extends Stmt{
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

    public final Expr condition;
    public final Stmt thenBranch;
    public final List<ElseIf> elseIfBranch;
    public final Stmt elseBranch;
  }
 public static class ElseIf extends Stmt{
    public ElseIf (Expr condition, Stmt thenBranch){
      this.condition = condition;
      this.thenBranch = thenBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitElseIfStmt(this);
    }

    public final Expr condition;
    public final Stmt thenBranch;
  }
 public static class Print extends Stmt{
    public Print (Expr expression){
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    public final Expr expression;
  }
 public static class Var extends Stmt{
    public Var (String dataType, Token name, Expr initializer){
      this.dataType = dataType;
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    public final String dataType;
    public final Token name;
    public final Expr initializer;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}

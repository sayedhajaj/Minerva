import java.util.*;

abstract class Stmt {
    interface Visitor <R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }

    static class Class extends Stmt {
        Class(Token name) {
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }

        final Token name;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression; 
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }
}
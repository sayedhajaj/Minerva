package frontend

import Type

sealed class Stmt {
    class Class(
        val name: Token,
        val superclass: Expr.Variable?,
        val constructor: Constructor,
        val methods: List<Function>,
        val fields: List<Stmt.Var>) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt()

    class Print(val expression: Expr): Stmt()

    class Expression(val expression: Expr) : Stmt()

    class Constructor(
        val fields: Map<Int, Token>,
        val parameters: List<Pair<Token, Type>>,
        val superArgs: List<Expr>,
        val constructorBody: Expr.Block
    ) : Stmt()
    class Function(val name: Token, val functionBody: Expr.Function): Stmt()

    class Var(val name: Token, val initializer: Expr, var type: Type): Stmt()

    class While(val condition: Expr, val body: Stmt): Stmt()
}
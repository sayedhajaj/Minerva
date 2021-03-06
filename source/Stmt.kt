sealed class Stmt {
    class Class(public val name: Token) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt()

    class Print(val expression: Expr): Stmt()

    class Expression(val expression: Expr) : Stmt()

    class Function(val name: Token, val functionBody: Expr.Function): Stmt()

    class Var(val name: Token, val initializer: Expr): Stmt()

    class While(val condition: Expr, val body: Stmt): Stmt()
}
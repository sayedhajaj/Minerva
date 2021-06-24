sealed class Stmt {
    class Block(val statements: List<Stmt>) : Stmt()

    class Class(public val name: Token) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt()

    class Print(val expression: Expr): Stmt()

    class Expression(val expression: Expr) : Stmt()

    class Var(val name: Token, val initializer: Expr): Stmt()

    class While(val condition: Expr, val body: Stmt): Stmt()
}
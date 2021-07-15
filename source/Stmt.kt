sealed class Stmt {
    class Class(
        val name: Token,
        val constructor: Constructor,
        val methods: List<Function>,
        val fields: Map<Token, Expr>) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt()

    class Print(val expression: Expr): Stmt()

    class Expression(val expression: Expr) : Stmt()

    class Constructor(
        val fields: Map<Int, Token>,
        val parameters: List<Token>,
        val constructorBody: Expr.Block
    ) : Stmt()
    class Function(val name: Token, val functionBody: Expr.Function): Stmt()

    class Var(val name: Token, val initializer: Expr): Stmt()

    class While(val condition: Expr, val body: Stmt): Stmt()
}
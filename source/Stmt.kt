sealed class Stmt {
    class Block(val statements: List<Stmt>) : Stmt()

    class Class(public val name: Token) : Stmt()

    class Print(val expression: Expr): Stmt()

    class Expression(val expression: Expr) : Stmt()
}
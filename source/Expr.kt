import Expr.Binary

sealed class Expr {
    class Assign(val name: Token, val value:Expr) : Expr()
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Block(val statements: List<Stmt>) : Expr()
    class Call(val callee: Expr, val arguments: List<Expr>): Expr()
    class Grouping(val expr: Expr): Expr()
    class Function(val parameters: List<Token>, val body: Expr): Expr()
    class If(val condition: Expr, val thenBranch: Expr, val elseBranch: Expr): Expr()
    class Literal(val value: Any?) : Expr()
    class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Unary(val operator: Token, val right: Expr) : Expr()
    class Variable(val name: Token): Expr()
}
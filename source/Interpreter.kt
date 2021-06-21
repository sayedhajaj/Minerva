class Interpreter(val statements: List<Stmt>) {
    fun interpet() {
        statements.forEach {
            execute(it)
        }

    }
    
    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {
                stmt.statements.forEach {
                    execute(it)
                }
            }
            is Stmt.Class -> {

            }
            is Stmt.Print -> {
                println(evaluate(stmt.expression))
            }
            is Stmt.Expression -> {
                evaluate(stmt.expression)
            }
        }
    }

    fun evaluate(expr: Expr): Any?  =  when (expr) {
        is Expr.Binary -> evaluateBinary(expr)
        is Expr.Grouping -> evaluate(expr.expr)
        is Expr.Literal -> expr.value
    }

    fun evaluateBinary(expr: Expr.Binary): Any? =
        when (expr.operator.type) {
            TokenType.PLUS ->
                (evaluate(expr.left) as Double) + (evaluate(expr.right) as Double)

            TokenType.MINUS -> (evaluate(expr.left) as Double) - (evaluate(expr.right) as Double)

            TokenType.GREATER -> (evaluate(expr.left) as Double) > (evaluate(expr.right) as Double)
            TokenType.GREATER_EQUAL -> (evaluate(expr.left) as Double) >= (evaluate(expr.right) as Double)
            TokenType.LESS -> (evaluate(expr.left) as Double) < (evaluate(expr.right) as Double)
            TokenType.LESS_EQUAL -> (evaluate(expr.left) as Double) <= (evaluate(expr.right) as Double)
            TokenType.EQUAL_EQUAL -> evaluate(expr.left) == evaluate(expr.right)
            TokenType.BANG_EQUAL -> evaluate(expr.left) != evaluate(expr.right)

            else -> null
        }
}

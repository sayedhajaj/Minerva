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
        is Expr.Unary -> evaluateUnary(expr)
    }

    fun evaluateUnary(expr: Expr.Unary): Any?  {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.MINUS -> -(right as Double)
            TokenType.BANG -> !(right as Boolean)
            else -> null
        }
    }

    fun evaluateBinary(expr: Expr.Binary): Any?  {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.PLUS ->
                (left as Double) + (right as Double)

            TokenType.MINUS -> (left as Double) - (right as Double)
            TokenType.SLASH -> (left as Double) / (right as Double)
            TokenType.STAR -> (left as Double) * (right as Double)

            TokenType.GREATER -> (left as Double) > (right as Double)
            TokenType.GREATER_EQUAL -> (left as Double) >= (right as Double)
            TokenType.LESS -> (left as Double) < (right as Double)
            TokenType.LESS_EQUAL -> (left as Double) <= (right as Double)
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.BANG_EQUAL -> expr != right

            else -> null
        }
    }
}

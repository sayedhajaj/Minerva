class Interpreter(val statements: List<Stmt>) {
    fun interpet() {
        statements.forEach {
            execute(it)
        }

    }
    
    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {

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
        is Expr.Binary -> (evaluate(expr.left)  as Double) + (evaluate(expr.right) as Double)
        is Expr.Literal -> expr.value
    }
}

class Interpreter(val statements: List<Stmt>) {

    var environment = Environment()

    fun interpet() {
        statements.forEach {
            execute(it)
        }

    }
    
    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {
                executeBlock(stmt.statements, Environment(environment))
            }
            is Stmt.Class -> {

            }
            is Stmt.If -> {
                if (evaluate(stmt.condition) == true) {
                    execute(stmt.thenBranch)
                } else {
                    stmt.elseBranch?.let { execute(it) }
                }
            }
            is Stmt.Print -> {
                println(evaluate(stmt.expression))
            }
            is Stmt.Var -> {
                val value = evaluate(stmt.initializer)
                environment.define(stmt.name.lexeme, value)
            }
            is Stmt.Expression -> {
                evaluate(stmt.expression)
            }
            is Stmt.While -> {
                while (evaluate(stmt.condition) == true) {
                    execute(stmt.body)
                }
            }
        }
    }

    fun evaluate(expr: Expr): Any?  =  when (expr) {
        is Expr.Assign -> evaluateAssign(expr)
        is Expr.Binary -> evaluateBinary(expr)
        is Expr.Grouping -> evaluate(expr.expr)
        is Expr.Literal -> expr.value
        is Expr.Unary -> evaluateUnary(expr)
        is Expr.Variable -> evaluateVariable(expr)
        is Expr.Logical -> evaluateLogical(expr)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment

        this.environment = environment

        statements.forEach {
            execute(it)
        }
        this.environment = previous
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

    fun evaluateVariable(variable: Expr.Variable): Any? =
        environment.get(variable.name)

    fun evaluateAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    fun evaluateLogical(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (left == true) return left
        } else {
            if (left == false) return left
        }
        return evaluate(expr.right)
    }
}

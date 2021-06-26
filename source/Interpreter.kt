class Interpreter(val statements: List<Stmt>, val locals: MutableMap<Expr, Int>) {

    val globals = Environment()
    var environment = globals

    fun interpet() {
        statements.forEach {
            execute(it)
        }

    }
    
    fun execute(stmt: Stmt) {
        when (stmt) {
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
            is Stmt.Function -> {
                environment.define(stmt.name.lexeme, MinervaFunction(
                    stmt.functionBody.parameters,
                    stmt.functionBody.body,
                    environment)
                )
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
        is Expr.Block -> executeBlock(expr.statements, Environment(environment))
        is Expr.Assign -> evaluateAssign(expr)
        is Expr.Binary -> evaluateBinary(expr)
        is Expr.Grouping -> evaluate(expr.expr)
        is Expr.If -> {
            if (evaluate(expr.condition) == true) {
                evaluate(expr.thenBranch)
            } else {
                evaluate(expr.elseBranch)
            }
        }
        is Expr.Literal -> expr.value
        is Expr.Unary -> evaluateUnary(expr)
        is Expr.Variable -> evaluateVariable(expr)
        is Expr.Logical -> evaluateLogical(expr)
        is Expr.Call -> evaluateCall(expr)
        is Expr.Function -> MinervaFunction(expr.parameters, expr.body, environment)
    }

    fun evaluateCall(expr: Expr.Call) : Any? {
        val callee = evaluate(expr.callee)

        val arguments = expr.arguments.map { evaluate(it) }

        if (callee is MinervaCallable) {
            return callee.call(this, arguments)
        }
        return null
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment): Any? {
        val previous = this.environment

        var lastExpr: Any? = null

        this.environment = environment

        statements.forEach {
            if (it is Stmt.Expression) {
                lastExpr = evaluate(it.expression)
            } else {
                execute(it)
            }
        }
        this.environment = previous

        return lastExpr
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
        lookUpVariable(variable.name, variable)

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    fun evaluateAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
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

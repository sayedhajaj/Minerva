import java.util.*

class Resolver {

    val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    val locals: MutableMap<Expr, Int> = mutableMapOf()

    fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    fun resolve(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> {
                resolve(stmt.expression)
            }
            is Stmt.Class -> {}
            is Stmt.Function -> {
                declare(stmt.name)
                define(stmt.name)
                resolveFunctionBody(stmt.functionBody)
            }
            is Stmt.If -> {
                resolve(stmt.condition)
                resolve(stmt.thenBranch)
                if (stmt.elseBranch != null) resolve(stmt.elseBranch)
            }
            is Stmt.Print -> {
                resolve(stmt.expression)
            }
            is Stmt.Var -> {
                declare(stmt.name)
                resolve(stmt.initializer)
                define(stmt.name)
            }
            is Stmt.While -> {
                resolve(stmt.condition)
                resolve(stmt.body)
            }
        }
    }

    private fun resolveFunctionBody(functionBody: Expr.Function) {
        if (functionBody.body !is Expr.Block) {
            beginScope()
        }
        functionBody.parameters.forEach {
            declare(it)
            define(it)
        }
        resolve(functionBody.body)
        if (functionBody.body !is Expr.Block) {
            endScope()
        }
    }

    fun resolve(expr: Expr) {
        when (expr) {
            is Expr.Block -> {
                beginScope()
                resolve(expr.statements)
                endScope()
            }
            is Expr.Assign -> {
                resolve(expr.value)
                resolveLocal(expr, expr.name)
            }
            is Expr.Binary -> {
                resolve(expr.left)
                resolve(expr.right)
            }
            is Expr.Call -> {
                resolve(expr.callee)
                expr.arguments.forEach {
                    resolve(it)
                }
            }
            is Expr.Function -> {
                resolveFunctionBody(expr)
            }
            is Expr.Grouping -> resolve(expr.expr)
            is Expr.If -> {
                resolve(expr.condition)
                resolve(expr.thenBranch)
                resolve(expr.elseBranch)
            }
            is Expr.Literal -> {}
            is Expr.Logical -> {
                resolve(expr.left)
                resolve(expr.right)
            }
            is Expr.Unary -> resolve(expr.right)
            is Expr.Variable -> {
                if (scopes.isNotEmpty() && scopes.peek()[expr.name.lexeme] == false) {
                    // error
                }
                resolveLocal(expr, expr.name)
            }
        }
    }

    fun beginScope() {
        scopes.push(mutableMapOf())
    }

    fun endScope() {
        scopes.pop()
    }

    fun declare(name: Token) {
        if (scopes.empty()) return
        val scope = scopes.peek()
        scope[name.lexeme] = false
    }

    fun define(name: Token) {
        if (scopes.empty()) return
        scopes.peek()[name.lexeme] = true
    }

    fun resolveLocal(expr: Expr, name: Token) {
        for (i in (scopes.size-1) downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                val depth = scopes.size - 1 - i
                locals[expr] = depth
                return
            }
        }
    }
}
package frontend.analysis

import frontend.Expr
import frontend.Stmt
import frontend.Token

class Resolver {

    val scopes: ArrayDeque<MutableMap<String, Boolean>> = ArrayDeque()
    val locals: MutableMap<Expr, Int> = mutableMapOf()

    fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    fun resolve(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> {
                resolve(stmt.expression)
            }
            is Stmt.Class -> {
                declare(stmt.name)
                define(stmt.name)

                if (stmt.superclass != null) {
                    resolve(stmt.superclass)
                    beginScope()

                    scopes.last()["super"] = true
                }

                beginScope()
                scopes.last().put("this", true)

                resolve(stmt.constructor)

                stmt.fields.forEach {
                    declare(it.name)
                    define(it.name)
                    resolve(it.initializer)
                }

                stmt.methods.forEach {
                    resolve(it.function)
                }


                endScope()

                if (stmt.superclass != null) endScope()

            }
            is Stmt.Function -> {
                declare(stmt.name)
                define(stmt.name)
                resolveFunctionBody(stmt.functionBody)
            }
            is Stmt.Print -> {
                resolve(stmt.expression)
            }
            is Stmt.Var -> {
                declare(stmt.name)
                resolve(stmt.initializer)
                define(stmt.name)
            }
            is Stmt.Constructor -> {
                stmt.fields.values.forEach {
                    declare(it)
                    define(it)
                }
//                beginScope()
                stmt.parameters.forEach {
                    declare(it.first)
                    define(it.first)
                }



                stmt.superArgs.forEach { resolve(it) }

                resolve(stmt.constructorBody.statements)
//                endScope()
            }
            is Stmt.Destructure -> {
                stmt.names.forEach {
                    declare(it.name)
                }
                resolve(stmt.initializer)
                stmt.names.forEach {
                    define(it.name)
                }
            }
            is Stmt.Module -> {
                declare(stmt.name)
                define(stmt.name)

                beginScope()

                stmt.classes.forEach { resolve(it) }
                stmt.enums.forEach { resolve(it) }
                stmt.functions.forEach { resolve(it) }
                stmt.fields.forEach { resolve(it) }

                endScope()
            }
            else -> {}
        }
    }

    private fun resolveFunctionBody(functionBody: Expr.Function) {
//        if (functionBody.body !is frontend.Expr.Block) {
            beginScope()
//        }
        functionBody.parameters.forEach {
            declare(it)
            define(it)
        }
        if (functionBody.body is Expr.Block) {
            resolve(functionBody.body.statements)
        } else {
            resolve(functionBody.body)
        }
//        if (functionBody.body !is frontend.Expr.Block) {
            endScope()
//        }
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
                if (expr.elseBranch != null)
                    resolve(expr.elseBranch)
            }
            is Expr.While -> {
                resolve(expr.condition)
                resolve(expr.body)
            }
            is Expr.ForEach -> {
                resolve(expr.iterable)
                beginScope()
                declare(expr.name)
                define(expr.name)
                resolve(expr.body)
                endScope()
            }
            is Expr.Literal -> {}
            is Expr.Logical -> {
                resolve(expr.left)
                resolve(expr.right)
            }
            is Expr.Unary -> resolve(expr.right)
            is Expr.Variable -> {
                if (scopes.isNotEmpty() && scopes.first()[expr.name.lexeme] == false) {
                    // error
                }
                resolveLocal(expr, expr.name)
            }
            is Expr.Get -> {
                resolve(expr.obj)
                if (expr.index != null) {
                    resolve(expr.index)
                }
            }
            is Expr.Set -> {
                resolve(expr.value)
                resolve(expr.obj)
                if (expr.index != null) {
                    resolve(expr.index)
                }
            }
            is Expr.This -> resolveLocal(expr, expr.keyword)
            is Expr.Super -> resolveLocal(expr, expr.keyword)
            is Expr.Array -> {
                expr.values.forEach { resolve(it) }
            }
            is Expr.TypeMatch -> {
                resolve(expr.variable)
                expr.conditions.forEach {
                    beginScope()
                    declare(expr.variable.name)
                    define(expr.variable.name)
                    if (it.third != null) {
                        declare(it.third!!)
                        define(it.third!!)
                    }
                    val body = it.second
                    if (body is Expr.Block) {
                        resolve(body.statements)
                    } else {
                        resolve(body)
                    }

                    endScope()
                }
                if (expr.elseBranch != null) {
                    resolve(expr.elseBranch)
                }
            }
            is Expr.Match -> {
                resolve(expr.expr)
                expr.branches.forEach {
                    resolve(it.first)
                    resolve(it.second)
                }
                resolve(expr.elseBranch)
            }
            is Expr.Tuple -> {
                expr.values.forEach { resolve(it) }
            }
        }
    }

    fun beginScope() {
        scopes.add(mutableMapOf())
    }

    fun endScope() {
        scopes.removeLast()
    }

    fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.last()
        scope[name.lexeme] = false
    }

    fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.last()[name.lexeme] = true
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
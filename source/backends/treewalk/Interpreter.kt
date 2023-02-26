package backends.treewalk

import frontend.Environment
import frontend.Type
import frontend.TypeChecker
import frontend.Expr
import frontend.Stmt
import frontend.Token
import frontend.TokenType

class Interpreter(val statements: List<Stmt>, val locals: MutableMap<Expr, Int>, val typeChecker: TypeChecker) {

    val globals = Environment()
    var environment = globals
    val printStatements = mutableListOf<String>()

    init {
        globals.define("Array", object : MinervaCallable {
            override fun arity(): Int = 2

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val size = arguments[0] as Int
                val initialiser = arguments[1] as MinervaCallable
                val arr = Array(size) { index ->
                    initialiser.call(this@Interpreter, listOf(index))

                }
                return MinervaArray(arr, this@Interpreter)
            }
        })

        globals.define("square", object : MinervaCallable {
            override fun arity(): Int = 1

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val num = arguments[0] as Int
                return num * num
            }
        })

        globals.define("Map", object : MinervaCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return MinervaMap(interpreter)
            }
        })
    }

    fun interpet() {
        statements.forEach {
            execute(it)
        }

    }

    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Class -> {
                environment.define(stmt.name.lexeme, null)

                var superClass: MinervaClass? = null

                if (stmt.superclass != null) {
                    superClass = evaluate(stmt.superclass) as MinervaClass

                    environment = Environment(environment)
                    environment.define("super", superClass)
                }

//                stmt.constructor.fields.forEach {  ->  }

                val methods = stmt.methods.associate {
                    it.function.name.lexeme to MinervaFunction(it.function.name.lexeme, it.function.functionBody, environment)
                }

                val fields = stmt.fields.associate {
                    it.name.lexeme to it.initializer
                }

                val constructor = MinervaConstructor(
                    stmt.constructor.fields, stmt.constructor.parameters.map { it.first },
                    stmt.constructor.constructorBody, environment
                )

                val klass =
                    MinervaClass(stmt.name.lexeme, superClass, stmt.constructor.superArgs, constructor, methods, fields)

                if (superClass != null) {
                    environment = environment.enclosing!!
                }

                environment.assign(stmt.name, klass)
            }
            is Stmt.If -> {
                if (evaluate(stmt.condition) == true) {
                    execute(stmt.thenBranch)
                } else {
                    stmt.elseBranch?.let { execute(it) }
                }
            }
            is Stmt.Print -> {
                log(evaluate(stmt.expression).toString())
            }
            is Stmt.Var -> {
                val value = evaluate(stmt.initializer)
                environment.define(stmt.name.lexeme, value)
            }
            is Stmt.Function -> {
                environment.define(
                    stmt.name.lexeme, MinervaFunction(
                        stmt.name.lexeme,
                        stmt.functionBody,
                        environment
                    )
                )
            }
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.While -> {
                while (evaluate(stmt.condition) == true) {
                    execute(stmt.body)
                }
            }
            is Stmt.Constructor -> { }
            is Stmt.ClassDeclaration -> { }
            is Stmt.ConstructorDeclaration -> { }
            is Stmt.FunctionDeclaration -> { }
            is Stmt.Interface -> { }
            is Stmt.PrintType -> log(stmt.expression.type.toString())
            is Stmt.VarDeclaration -> { }
            is Stmt.Enum -> {
                environment.define(stmt.name.lexeme, MinervaEnum(stmt.members))
            }
            is Stmt.Destructure -> {
                val value = evaluate(stmt.initializer)
                if (value is MinervaTuple) {
                    stmt.names.forEachIndexed { index, varDeclaration ->
                        environment.define(varDeclaration.name.lexeme, value.elements[index])
                    }
                }
        }
        }
    }

    fun evaluate(expr: Expr): Any? = when (expr) {
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
        is Expr.Function -> MinervaFunction("", expr, environment)
        is Expr.Get -> {
            val obj = evaluate(expr.obj)
            if (obj is MinervaArray && expr.index != null) {
                val index = evaluate(expr.index) as Int
                obj.get(index)
            } else if (obj is MinervaEnum) obj.get(expr.name)
            else if (obj is MinervaInstance) obj.get(expr.name)
            else null
        }
        is Expr.Set -> {
            val obj = evaluate(expr.obj)
            if (obj is MinervaArray && expr.index != null) {
                obj.set((evaluate(expr.index) as Int), evaluate(expr.value))
            } else if (obj is MinervaInstance) {
                val value = evaluate(expr.value)
                obj.set(expr.name, value)
                value
            } else null
        }
        is Expr.This -> lookUpVariable(expr.keyword, expr)
        is Expr.Super -> {
            val distance = locals[expr]
            val superclass = distance?.let { environment.getAt(it, "super") } as MinervaClass
            val obj = environment.getAt(distance - 1, "this") as MinervaInstance
            val method = superclass.findMethod(expr.method.lexeme)
            method?.bind(obj)
        }
        is Expr.Array -> {
            val values = expr.values.map { evaluate(it) }
            MinervaArray(values.toTypedArray(), this)
        }
        is Expr.TypeMatch -> {
            val value = lookUpVariable(expr.variable.name, expr.variable)
            val valueType = getValueType(value)
            val matching = expr.conditions.filter {
                it.first.canAssignTo(valueType, typeChecker)
            }

            var result: Any? = null

            if (matching.isNotEmpty()) {
                val branch = matching[0].second
                val closure = Environment(this.environment)
                closure.define(expr.variable.name.lexeme, value)
                val alias = matching[0].third
                if (alias != null) {
                    closure.define(alias.lexeme, value)
                }
                val block = if (branch is Expr.Block) {
                    branch
                } else {
                    Expr.Block(listOf(Stmt.Expression(branch)))
                }
                result = executeBlock(block.statements, closure)
            } else {
                if (expr.elseBranch != null) result = evaluate(expr.elseBranch)
            }

            result
        }
        is Expr.Match -> {
            val value = evaluate(expr.expr)
            val matching = expr.branches.filter {
                value == evaluate(it.first)
            }

            val result = if (matching.isNotEmpty())
                evaluate(matching[0].second)
            else
                evaluate(expr.elseBranch)
            result
        }
        is Expr.Tuple -> {
            val values = expr.values.map { evaluate(it) }
            MinervaTuple(values.toTypedArray(), this)
        }
    }

    private fun getValueType(value: Any?): Type = when (value) {
        is Int -> Type.IntegerType()
        is Double -> Type.DoubleType()
        is Boolean -> Type.BooleanType()
        is String -> Type.StringType()
        null -> Type.NullType()
        is MinervaArray -> {
            val elementTypes = value.elements.map { getValueType(it) }
            typeChecker.createArrayType(typeChecker.flattenTypes(elementTypes))
        }
        is MinervaInstance -> {
            var className = Expr.Variable(Token(TokenType.IDENTIFIER, value.klass?.name ?: "null", null, -10))
            val instance = typeChecker.lookUpVariableType(className.name, className) as Type.InstanceType
            val argMap = mutableMapOf<String, Type>()
            instance.typeParams.forEach {
                argMap[it.identifier.name.lexeme] = getValueType(value.fields[it.identifier.name.lexeme])
            }
            typeChecker.resolveTypeArgument(argMap, instance)
        }
        is MinervaFunction -> value.declaration.type
        else -> Type.NullType()
    }

    fun evaluateCall(expr: Expr.Call): Any? {
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

    fun evaluateUnary(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        if (right is MinervaInstance) {
            val unaryMethods = mapOf(TokenType.PLUS to "plus", TokenType.MINUS to "minus", TokenType.BANG to "not")
            val operatorName = unaryMethods[expr.operator.type]!!
            val token =  Token(TokenType.IDENTIFIER, operatorName, operatorName, -1)
            val method = right.get(token) as MinervaCallable
            return method.call(this, listOf(right))
        }

        return when (expr.operator.type) {
            TokenType.MINUS -> -(right as Double)
            TokenType.BANG -> !(right as Boolean)
            else -> null
        }
    }

    fun evaluateBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.left.type) {
            is Type.StringType -> {
                when (expr.operator.type) {
                    TokenType.PLUS -> (left as String) + (right as String)
                    TokenType.EQUAL_EQUAL -> left == right
                    TokenType.BANG_EQUAL -> left != right
                    else -> null
                }
            }
            is Type.IntegerType -> {
                when (expr.operator.type) {
                    TokenType.PLUS -> (left as Int) + (right as Int)
                    TokenType.MINUS -> (left as Int) - (right as Int)
                    TokenType.SLASH -> (left as Int) / (right as Int)
                    TokenType.STAR -> (left as Int) * (right as Int)

                    TokenType.GREATER -> (left as Int) > (right as Int)
                    TokenType.GREATER_EQUAL -> (left as Int) >= (right as Int)
                    TokenType.LESS -> (left as Int) < (right as Int)
                    TokenType.LESS_EQUAL -> (left as Int) <= (right as Int)
                    TokenType.EQUAL_EQUAL -> left == right
                    TokenType.BANG_EQUAL -> left != right

                    else -> null
                }
            }

            is Type.DoubleType -> {
                when (expr.operator.type) {
                    TokenType.PLUS -> (left as Double) + (right as Double)

                    TokenType.MINUS -> (left as Double) - (right as Double)
                    TokenType.SLASH -> (left as Double) / (right as Double)
                    TokenType.STAR -> (left as Double) * (right as Double)

                    TokenType.GREATER -> (left as Double) > (right as Double)
                    TokenType.GREATER_EQUAL -> (left as Double) >= (right as Double)
                    TokenType.LESS -> (left as Double) < (right as Double)
                    TokenType.LESS_EQUAL -> (left as Double) <= (right as Double)
                    TokenType.EQUAL_EQUAL -> left == right
                    TokenType.BANG_EQUAL -> left != right

                    else -> null
                }
            }

            is Type.BooleanType -> when (expr.operator.type) {
                TokenType.EQUAL_EQUAL -> left == right
                TokenType.BANG_EQUAL -> left != right
                else -> null
            }
            is Type.InstanceType -> {
                val obj = left as MinervaInstance
                val operatorMethods = mapOf(
                    TokenType.PLUS to "add",
                    TokenType.MINUS to "subtract",
                    TokenType.SLASH to "divide",
                    TokenType.STAR to "multiply",
                )

                val comparisonOperators = listOf(
                    TokenType.LESS,
                    TokenType.LESS_EQUAL,
                    TokenType.GREATER,
                    TokenType.GREATER_EQUAL,
                    TokenType.EQUAL_EQUAL,
                    TokenType.BANG_EQUAL
                )

                val operatorName = operatorMethods[expr.operator.type]
                if (operatorName != null) {
                    val token =  Token(TokenType.IDENTIFIER, operatorName, operatorName, -1)
                    val method = obj.get(token) as MinervaCallable
                    return method.call(this, listOf(right))
                } else {
                    if (expr.operator.type in comparisonOperators) {
                        val compareMethodName  =  Token(TokenType.IDENTIFIER, "compareTo", "compareTo", -1)
                        val compareMethod = obj.get(compareMethodName) as MinervaCallable?
                        if (compareMethod != null) {

                            val result = compareMethod.call(this, listOf(right)) as Int
                            return when (expr.operator.type) {
                                TokenType.GREATER ->  result > 0
                                TokenType.GREATER_EQUAL ->  result >= 0
                                TokenType.LESS -> result < 0
                                TokenType.LESS_EQUAL -> result <= 0
                                TokenType.EQUAL_EQUAL -> result == 0
                                TokenType.BANG_EQUAL -> result != 0
                                else -> false
                            }
                        } else {
                            val equalMethodName = Token(TokenType.IDENTIFIER, "equals", "equals", -1)
                            val equalsMethod = obj.get(equalMethodName) as MinervaCallable
                            val result = equalsMethod.call(this, listOf(right)) as Boolean
                            return if (expr.operator.type == TokenType.EQUAL_EQUAL) result
                            else !result
                        }
                    }
                }
                return null
            }
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

    fun log(value: String) {
        printStatements.add(value)
        println(value)
    }
}

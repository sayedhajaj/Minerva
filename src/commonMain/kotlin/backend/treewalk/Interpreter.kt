package backend.treewalk

import backend.treewalk.natives.*
import frontend.analysis.Environment
import frontend.Type
import frontend.Expr
import frontend.Stmt
import frontend.Token
import frontend.TokenType
import frontend.analysis.ITypeChecker

class Interpreter(var locals: MutableMap<Expr, Int>, val typeChecker: ITypeChecker) {

    val globals = Environment()
    var environment = globals
    val printStatements = mutableListOf<String>()

    init {
        globals.define("Array", object : MinervaCallable {
            override fun arity(): Int = 2

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val size = arguments[0] as MinervaInteger
                val initialiser = arguments[1] as MinervaCallable
                val arr = Array(size.value) { index ->
                    initialiser.call(this@Interpreter, listOf(index))

                }
                return MinervaArray(arr, this@Interpreter)
            }
        })

        globals.define("square", object : MinervaCallable {
            override fun arity(): Int = 1

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val num = arguments[0] as MinervaInteger
                return num.value * num.value
            }
        })

        globals.define("Map", object : MinervaCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return MinervaMap(interpreter)
            }
        })

        globals.define("Example", MinervaModule(members = mapOf(
            "NAME" to MinervaString("Example", this),
            "view" to object : MinervaCallable {
                override fun arity(): Int = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaString("This is an example", this@Interpreter)
                }
            }
        ), this))

        globals.define("Math", MinervaMath(this))


        globals.define("Random", MinervaRandom(this))
    }

    fun defineGlobal(name: String, value: Any?) {
        globals.define(name, value)
    }

    fun interpet(statements: List<Stmt>) {
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
                    it.function.name.lexeme to MinervaFunction(
                        it.function.name.lexeme,
                        it.function.functionBody,
                        environment
                    )
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
            is Stmt.PrintType -> log(stmt.expression.type.toString())
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
            is Stmt.Module -> {
                val members = mutableMapOf<String, Any?>()

                val previous = this.environment

                this.environment = Environment(this.environment)

                stmt.fields.forEach {
                    execute(it)
                    environment.get(it.name)?.let { field ->
                        members[it.name.lexeme] = field
                    }
                }

                stmt.classes.forEach {
                    execute(it)
                    environment.get(it.name)?.let { cls -> members[it.name.lexeme] = cls }
                }

                stmt.functions.forEach {
                    execute(it)
                    environment.get(it.name)?.let { fn -> members[it.name.lexeme] = fn }
                }

                stmt.enums.forEach {
                    execute(it)
                    environment.get(it.name)?.let { e -> members[it.name.lexeme] = e }
                }

                stmt.modules.forEach {
                    execute(it)
                    environment.get(it.name)?.let { module -> members[it.name.lexeme] = module }
                }

                this.environment = previous

                environment.define(stmt.name.lexeme, MinervaModule(members, this))
            }
            else -> {}
        }
    }

    fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Block -> executeBlock(expr.statements, Environment(environment))
        is Expr.Assign -> evaluateAssign(expr)
        is Expr.Binary -> evaluateBinary(expr)
        is Expr.Grouping -> evaluate(expr.expr)
        is Expr.If -> {
            val condition = evaluate(expr.condition) as MinervaBoolean
            if (condition.value) {
                evaluate(expr.thenBranch)
            } else {
                if (expr.elseBranch != null)
                    evaluate(expr.elseBranch)
                else
                    null
            }
        }
        is Expr.While -> {
            val outputs = mutableListOf<Any?>()
            while ((evaluate(expr.condition) as MinervaBoolean).value) {
                outputs.add(evaluate(expr.body))
            }
            MinervaArray(outputs.toTypedArray(), this)
        }
        is Expr.For -> {
            val outputs = mutableListOf<Any?>()
            val previous = this.environment
            this.environment = Environment(this.environment)

            execute(expr.initializer)
            while ((evaluate(expr.condition) as MinervaBoolean).value) {
                outputs.add(evaluate(expr.body))
                evaluate(expr.increment)
            }
            this.environment = previous
            MinervaArray(outputs.toTypedArray(), this)
        }
        is Expr.ForEach -> {
            val outputs = mutableListOf<Any?>()
            val iterable = evaluate(expr.iterable) as MinervaInstance
            val iteratorFunction =
                iterable.get(Token(TokenType.IDENTIFIER, "iterator", "iterator", -1)) as MinervaCallable
            val iterator = iteratorFunction.call(this, emptyList()) as MinervaInstance
            val hasNext = iterator.get(Token(TokenType.IDENTIFIER, "hasNext", "hasNext", -1)) as MinervaCallable

            val previous = this.environment
            this.environment = Environment(this.environment)

            while ((hasNext.call(this, emptyList()) as MinervaBoolean).value) {
                val getNext = iterator.get(Token(TokenType.IDENTIFIER, "next", "next", -1)) as MinervaCallable
                val current = getNext.call(this, emptyList())
                environment.define(expr.name.lexeme, current)
                outputs.add(evaluate(expr.body))
            }

            this.environment = previous
            MinervaArray(outputs.toTypedArray(), this)
        }
        is Expr.Literal -> {
            when (expr.value) {
                is Int -> {
                    when (expr.tokenType) {
                        TokenType.DECIMAL -> {
                            MinervaDecimal(expr.value as Double, this)
                        }
                        TokenType.INTEGER -> {
                            MinervaInteger(expr.value, this)
                        }
                        else -> expr.value
                    }
                }
                is Double -> {
                    when (expr.tokenType) {
                        TokenType.DECIMAL -> {
                            MinervaDecimal(expr.value, this)
                        }
                        TokenType.INTEGER -> {
                            MinervaInteger(expr.value as Int, this)
                        }
                        else -> expr.value
                    }
                }
                is String -> MinervaString(expr.value, this)
                is Boolean -> MinervaBoolean(expr.value, this)
                is Char -> MinervaChar(expr.value, this)
                is MinervaInteger, is MinervaDecimal -> expr.value

                else -> expr.value
            }
        }
        is Expr.Unary -> evaluateUnary(expr)
        is Expr.Variable -> evaluateVariable(expr)
        is Expr.Logical -> evaluateLogical(expr)
        is Expr.Call -> evaluateCall(expr)
        is Expr.Function -> MinervaFunction("", expr, environment)
        is Expr.Get -> {
            val obj = evaluate(expr.obj)
            if (obj is MinervaInstance) {
                if (expr.index == null) {
                    obj.get(expr.name)
                } else {
                    val index = evaluate(expr.index)
                    val getMethod = obj.get(Token(TokenType.IDENTIFIER, "get", "get", -1)) as MinervaCallable
                    getMethod.call(this, listOf(index))
                }
            } else if (obj is MinervaEnum) obj.get(expr.name)
            else null
        }
        is Expr.Set -> {
            val obj = evaluate(expr.obj)
            if (obj is MinervaInstance) {
                if (expr.index == null) {
                    val value = evaluate(expr.value)
                    obj.set(expr.name, value)
                    value
                } else {
                    val setMethod = obj.get(Token(TokenType.IDENTIFIER, "set", "set", -1)) as MinervaCallable
                    setMethod.call(this, listOf(evaluate(expr.index), evaluate(expr.value)))
                }
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
                it.first.canAssignTo(valueType)
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
                if (value is MinervaInteger) {
                    value.value == (evaluate(it.first) as MinervaInteger).value
                } else {
                    value == evaluate(it.first)
                }
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
        null -> Type.NullType()
        is MinervaArray -> {
            val elementTypes = value.elements.map { getValueType(it) }
            typeChecker.createArrayType(typeChecker.flattenTypes(elementTypes))
        }
        is MinervaInstance -> {
            var className = Expr.Variable(Token(TokenType.IDENTIFIER, value.klass?.name ?: "null", null, -10))
            val instance = typeChecker.lookupInstance(className.name)
            val argMap = mutableMapOf<String, Type>()
            instance.typeParams.forEach {
                argMap[it.identifier.name.lexeme] = getValueType(value.fields[it.identifier.name.lexeme])
            }
            instance.resolveTypeArguments(argMap)
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
            val unaryMethods = mapOf(
                TokenType.PLUS to "plus",
                TokenType.MINUS to "minus",
                TokenType.BANG to "not",
                TokenType.PLUS_PLUS to "inc",
                TokenType.MINUS_MINUS to "dec"
            )
            val operatorName = unaryMethods[expr.operator.type]!!
            val token = Token(TokenType.IDENTIFIER, operatorName, operatorName, -1)
            val method = right.get(token) as MinervaCallable
            val result = method.call(this, listOf(right))
            if (expr.operator.type in listOf(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
                if (expr.right is Expr.Variable) {
                    environment.assign(expr.right.name, result)
                }
                if (expr.postfix) return right
            }

            return result
        }

        return null
    }

    fun evaluateBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        val obj = left as MinervaInstance
        val operatorMethods = mapOf(
            TokenType.PLUS to "add",
            TokenType.MINUS to "subtract",
            TokenType.SLASH to "divide",
            TokenType.STAR to "multiply",
            TokenType.MODULO to "rem",
            TokenType.POWER to "power"
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
            val token = Token(TokenType.IDENTIFIER, operatorName, operatorName, -1)
            val method = obj.get(token) as MinervaCallable
            return method.call(this, listOf(right))
        } else {
            if (expr.operator.type in comparisonOperators) {
                val compareMethodName = Token(TokenType.IDENTIFIER, "compareTo", "compareTo", -1)
                val compareMethod = obj.get(compareMethodName) as MinervaCallable?
                if (compareMethod != null) {

                    val result = compareMethod.call(this, listOf(right)) as MinervaInteger
                    val bool = when (expr.operator.type) {
                        TokenType.GREATER -> result.value > 0
                        TokenType.GREATER_EQUAL -> result.value >= 0
                        TokenType.LESS -> result.value < 0
                        TokenType.LESS_EQUAL -> result.value <= 0
                        TokenType.EQUAL_EQUAL -> result.value == 0
                        TokenType.BANG_EQUAL -> result.value != 0
                        else -> false
                    }
                    return MinervaBoolean(bool, this)
                } else {
                    val equalMethodName = Token(TokenType.IDENTIFIER, "equals", "equals", -1)
                    val equalsMethod = obj.get(equalMethodName) as MinervaCallable
                    val result = equalsMethod.call(this, listOf(right)) as MinervaBoolean
                    return MinervaBoolean(
                        if (expr.operator.type == TokenType.EQUAL_EQUAL) result.value
                        else !result.value, this
                    )
                }
            }
        }
        return null
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
        var value = evaluate(expr.value)

        val current = environment.get(expr.name) as MinervaInstance

        if (expr.operator.type in listOf(
                TokenType.PLUS_EQUAL,
                TokenType.MINUS_EQUAL,
                TokenType.SLASH_EQUAL,
                TokenType.STAR_EQUAL,
                TokenType.MODULO_EQUAL
            )
        ) {
            val operatorMethods = mapOf(
                TokenType.PLUS_EQUAL to "add",
                TokenType.MINUS_EQUAL to "subtract",
                TokenType.SLASH_EQUAL to "divide",
                TokenType.STAR_EQUAL to "multiply",
                TokenType.MODULO_EQUAL to "rem"
            )

            val operatorName = operatorMethods[expr.operator.type]

            if (operatorName != null) {
                val token = Token(TokenType.IDENTIFIER, operatorName, operatorName, -1)
                val method = current.get(token) as MinervaCallable
                value = method.call(this, listOf(value))
            }
        }

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

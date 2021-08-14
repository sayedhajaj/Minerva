import backends.treewalk.MinervaClass
import frontend.Expr
import frontend.Stmt
import frontend.Token
import frontend.TokenType

class TypeChecker(val locals: MutableMap<Expr, Int>) {

    val globals = Environment()
    var environment = globals

    val typeErrors: MutableList<String> = mutableListOf()

    init {
//        environment.define("Array", Type.InstanceType(
//            mutableMapOf(
//                "length" to Type.IntegerType(),
//                "map" to Type.FunctionType(listOf(Type.AnyType()), Type.AnyType())
//            ), null
//        ))
    }

    fun typeCheck(statements: List<Stmt>) {
        statements.forEach { typeCheck(it) }
    }

    fun typeCheck(stmt: Stmt) {
        when (stmt) {
            is Stmt.Class -> {
                val members = mutableMapOf<String, Type>()
                val params = mutableListOf<Type>()

                if (stmt.superclass != null) {
                    val superclass = lookUpVariableType(stmt.superclass.name, stmt.superclass) as Type.InstanceType
//                    environment = Environment(environment)

                    environment.define("super", superclass)
                }

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, members, stmt.superclass))

                stmt.fields.forEach {
                    typeCheck(it)
                    members[it.name.lexeme] = it.type
                }


                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, members, stmt.superclass))

                stmt.constructor.parameters.forEachIndexed {index, pair ->
                    params.add(pair.second)
                    if(stmt.constructor.fields.containsKey(index)) {
                        members[pair.first.lexeme] = pair.second
                    }
                }

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, members, stmt.superclass))

                stmt.methods.forEach {
                    typeCheck(it)
                    members[it.name.lexeme] = it.functionBody.type
                }


                typeCheck(stmt.constructor)

                environment.define(stmt.name.lexeme, Type.InstanceType(Expr.Variable(stmt.name), params, members, stmt.superclass))

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, members, stmt.superclass))
            }
            is Stmt.Constructor -> {
                val previous = environment
                environment = Environment(environment)

                stmt.parameters.forEachIndexed {index, pair ->
                    environment.define(pair.first.lexeme, pair.second)
                }
                getBlockType(stmt.constructorBody.statements, environment)
                environment = previous
            }
            is Stmt.Expression -> typeCheck(stmt.expression)
            is Stmt.Function -> {
                environment.define(stmt.name.lexeme, stmt.functionBody.type)
                val type = typeCheck(stmt.functionBody)
                environment.define(stmt.name.lexeme, type)
            }
            is Stmt.If -> {
                typeCheck(stmt.condition)
                typeCheck(stmt.thenBranch)
                if (stmt.condition.type !is Type.BooleanType) {
                    typeErrors.add("If condition should be boolean")
                }
                stmt.elseBranch?.let { typeCheck(it) }
            }
            is Stmt.Print -> {
                typeCheck(stmt.expression)
            }
            is Stmt.Var -> {
                val initialiserType = resolveInstanceType(stmt.type)
                val assignedType = typeCheck(stmt.initializer)
                val type = if(stmt.type is Type.InferrableType)
                    assignedType else initialiserType
                val canAssign = initialiserType.canAssignTo(assignedType, this)

                if (!canAssign) {
                    typeErrors.add("Cannot assign $assignedType to $initialiserType")
                }
                stmt.type = type
                environment.define(stmt.name.lexeme, type)
            }
            is Stmt.While -> {
                typeCheck(stmt.condition)

                if (stmt.condition.type !is Type.BooleanType) {
                    typeErrors.add("While condition should be boolean")
                }
                typeCheck(stmt.body)
            }
        }
    }

    private fun resolveInstanceType(type: Type): Type {
        return when (type) {
            is Type.InstanceType -> {
                lookUpVariableType(type.className.name, type.className)
            }
            is Type.ArrayType -> {
                Type.ArrayType(resolveInstanceType(type.type))
            }
            is Type.UnionType -> {
                Type.UnionType(type.types.map{resolveInstanceType(it)})
            }
            else -> type
        }
    }

    fun typeCheck(expr: Expr): Type {
        return when (expr) {
            is Expr.Get -> {
                typeCheck(expr.obj)
                if (expr.obj.type is Type.ArrayType) {
                    if(expr.index != null) {
                        typeCheck(expr.index)
                        if (expr.index.type !is Type.IntegerType) {
                            typeErrors.add("Array index should be an integer")
                        }
                    }
                    val thisType =  (expr.obj.type as Type.ArrayType).type
                    expr.type = thisType
                    return thisType
                }
                if (expr.obj is Expr.Variable) {
                    val className = (expr.obj).name
                    val calleeType = lookUpVariableType(className, expr.obj) as Type.InstanceType
                    val thisType = calleeType.getMemberType(expr.name.lexeme, this)
                    expr.type = thisType
                    thisType
                } else {
                    val calleeType = typeCheck(expr.obj) as Type.InstanceType
                    val thisType = calleeType.getMemberType(expr.name.lexeme, this)
                    expr.type = thisType
                    thisType
                }
            }
            is Expr.Array -> {
                val elementTypes = expr.values.map { typeCheck(it) }
                val type = flattenTypes(elementTypes)
                val thisType = Type.ArrayType(type)
                expr.type = thisType
                return thisType
            }
            is Expr.Assign -> {
                val left = lookUpVariableType(expr.name, expr)
                typeCheck(expr.value)
                if (!left.canAssignTo(expr.value.type, this)) {
                    typeErrors.add("Cannot assign ${expr.value.type} to ${left}")
                }
                val thisType = expr.value.type
                thisType
            }
            is Expr.Binary -> {
                val left = typeCheck(expr.left)
                val right = typeCheck(expr.right)


                val thisType = when (expr.operator.type) {
                    TokenType.PLUS -> left
                    TokenType.MINUS -> left
                    TokenType.SLASH -> left
                    TokenType.STAR -> left
                    TokenType.GREATER -> Type.BooleanType()
                    TokenType.GREATER_EQUAL -> Type.BooleanType()
                    TokenType.LESS -> Type.BooleanType()
                    TokenType.LESS_EQUAL -> Type.BooleanType()
                    TokenType.EQUAL_EQUAL -> Type.BooleanType()
                    TokenType.BANG_EQUAL -> Type.BooleanType()
                    else -> Type.NullType()
                }
                expr.type = thisType
                return thisType
            }
            is Expr.Block -> {
                val thisType = getBlockType(expr.statements, Environment(environment))
                expr.type = thisType
                thisType
            }
            is Expr.Call -> {
                val calleeType = typeCheck(expr.callee)
                expr.arguments.forEach { typeCheck(it) }
                if (calleeType is Type.FunctionType) {
                    calleeType.params.forEachIndexed{index, param ->
                        val argType = expr.arguments[index].type
                        if (!param.canAssignTo(argType, this)) {
                            typeErrors.add("Expected $param and got $argType")
                        }
                    }
                    val thisType = calleeType.result
                    expr.type = thisType

                    thisType
                } else if (calleeType is Type.InstanceType) {
                    calleeType.params.forEachIndexed{index, param ->
                        val argType = expr.arguments[index].type
                        if (!param.canAssignTo(argType, this)) {
                            typeErrors.add("Expected $param and got $argType")
                        }
                    }
                    expr.type = calleeType
                    expr.type
                } else {

                    calleeType
                }
            }
            is Expr.Function -> {
                val block = if (expr.body is Expr.Block)
                    expr.body
                else
                    Expr.Block(listOf(Stmt.Expression(expr.body)))

                val closure = Environment(environment)

                expr.parameters.forEachIndexed {index, token ->
                    closure.define(token.lexeme, resolveInstanceType((expr.type as Type.FunctionType).params[index]))
                }
                val returnType = getBlockType(block.statements, closure)
                if ((expr.type as Type.FunctionType).result is Type.InferrableType) {
                    expr.type = Type.FunctionType((expr.type as Type.FunctionType).params, returnType)
                }
                return expr.type
            }
            is Expr.Grouping -> {
                expr.type = typeCheck(expr.expr)
                expr.type
            }
            is Expr.If -> {
                typeCheck(expr.condition)
                if (expr.condition.type !is Type.BooleanType) {
                    typeErrors.add("If condition should be boolean")
                }
                val thenType = typeCheck(expr.thenBranch)
                val elseType = typeCheck(expr.elseBranch)
                val thisType = if (thenType::class == elseType::class) thenType else Type.UnionType(listOf(thenType, elseType))
                expr.type = thisType
                thisType
            }
            is Expr.Literal -> {
                val type = when (expr.value) {
                    is String -> {
                        Type.StringType()
                    }
                    is Double -> {
                        Type.DoubleType()
                    }
                    is Int -> {
                        Type.IntegerType()
                    }
                    is Boolean -> {
                        Type.BooleanType()
                    }
                    else -> {
                        Type.NullType()
                    }
                }
                expr.type = type
                expr.type
            }
            is Expr.Logical -> {
                val left = typeCheck(expr.left)
                val right = typeCheck(expr.right)
                if (left !is Type.BooleanType || right !is Type.BooleanType) {
                    typeErrors.add("Logical expression should have boolean left and right")
                }
                val thisType = Type.BooleanType()
                expr.type = thisType
                thisType
            }
            is Expr.Set -> {
                typeCheck(expr.obj)
                val left = expr.obj.type
                if (left is Type.ArrayType) {
                    typeCheck(expr.value)
                    if (expr.index != null) {
                        typeCheck(expr.index)
                        if (expr.index.type !is Type.IntegerType) {
                            typeErrors.add("Array index should be integer")
                        }
                    }
                    if (!left.type.canAssignTo(expr.value.type, this)) {
                        typeErrors.add("Cannot assign ${expr.value.type} to ${left.type}")
                    }
                        val thisType = expr.value.type
                        thisType
                } else {
                    val left = lookUpVariableType(expr.name, expr)
                    typeCheck(expr.value)
                    if (!left.canAssignTo(expr.value.type, this)) {
                        typeErrors.add("Cannot assign ${expr.value.type} to ${left}")
                    }
                    val thisType = expr.value.type
                    thisType
                }
            }
            is Expr.Super -> {
                val distance = locals[expr]
                val superclass = distance?.let { environment.getAt(it-1, "super") } as Type.InstanceType
                val thisType = (superclass).getMemberType(expr.method.lexeme, this)
                expr.type = thisType
                thisType
            }
            is Expr.This -> {
                val thisType = lookUpVariableType(expr.keyword, expr)
                expr.type = thisType
                thisType
            }
            is Expr.Unary -> {
                val thisType = typeCheck(expr.right)
                expr.type = thisType
                thisType
            }
            is Expr.Variable -> {
                val type = lookUpVariableType(expr.name, expr)
                expr.type = type
                type
            }
            is Expr.TypeMatch -> {
                typeCheck(expr.variable)
                if (expr.variable.type !is Type.AnyType && expr.variable.type !is Type.UnionType) {
                    typeErrors.add("Can only type match any and union types")
                }
                val types = mutableListOf<Type>()
                expr.conditions.forEach {

                    val block: Expr.Block = if (it.second is Expr.Block)
                        it.second as Expr.Block
                    else
                        Expr.Block(listOf(Stmt.Expression(it.second)))

                    val closure = Environment(environment)
                    closure.define(expr.variable.name.lexeme, it.first)

                    val returnType = getBlockType(block.statements, closure)
                    types.add(returnType)

                }
                if (expr.elseBranch != null) {
                    typeCheck(expr.elseBranch)
                    types.add(expr.elseBranch.type)
                }

                var hasElse = expr.elseBranch != null
                if (!isExhuastive(expr.variable.type, expr.conditions.map { it.first }, hasElse)) {
                    typeErrors.add("Typematch is not exhuastive")
                }
                expr.type = flattenTypes(types)
                expr.type
            }

            is Expr.Match -> {
                typeCheck(expr.expr)
                if (expr.expr.type !is Type.IntegerType && expr.expr.type !is Type.DoubleType && expr.expr.type !is Type.StringType) {
                    typeErrors.add("Can only use integer, double, or string types in match")
                }
                val types = mutableListOf<Type>()
                expr.branches.forEach {
                    typeCheck(it.first)
                    if (it.first.type::class != expr.expr.type::class) {
                        typeErrors.add("Conditions must be same type as match type")
                    }
                    typeCheck(it.second)
                    types.add(it.second.type)
                }
                typeCheck(expr.elseBranch)
                types.add(expr.elseBranch.type)
                expr.type = flattenTypes(types)
                expr.type
            }

        }
    }

    private fun isExhuastive(type: Type, branches: List<Type>, hasElse: Boolean): Boolean {
        if (hasElse) return true
        else {
            if (type is Type.AnyType) return false
            return if (type is Type.UnionType) {
                type.types.all {type -> branches.any {branch -> type.canAssignTo(branch, this) } }
            } else {
                false
            }
        }
    }

    fun flattenTypes(elementTypes: List<Type>): Type {
        val instanceTypes = elementTypes.filterIsInstance<Type.InstanceType>()
        val nonInstanceTypes = elementTypes.filter { it !is Type.InstanceType }.distinctBy { it::class }


        var constrainedInstances = mutableListOf<Type.InstanceType>()
        var i = 0
        while (i < instanceTypes.size - 1) {
            var j = i + 1
            while (j < instanceTypes.size) {
                var left = instanceTypes[i]
                var right = instanceTypes[j]
                if (left.canAssignTo(right, this)) constrainedInstances.add(left)
                if (right.canAssignTo(left, this)) constrainedInstances.add(right)
                j++
            }
            i++
        }
        val resultTypes = nonInstanceTypes + constrainedInstances
        val type = if (resultTypes.size == 1) resultTypes[0] else Type.UnionType(resultTypes)
        return type
    }

    fun lookUpVariableType(name: Token, expr: Expr): Type {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme) as Type
        } else {
            globals.get(name) as Type
        }
    }

    fun getBlockType(statements: List<Stmt>, environment: Environment): Type {
        val previous = this.environment
        var lastExpr: Type = Type.NullType()


        this.environment = environment

        statements.forEach {
            if (it is Stmt.Expression) {
                lastExpr = typeCheck(it.expression)
            }
            typeCheck(it)
        }

        this.environment = previous


        return lastExpr
    }
}
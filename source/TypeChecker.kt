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

                val typeParameters = stmt.constructor.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

                typeParameters.forEach {
                    environment.define(it.identifier.name.lexeme, it)
                }

                if (stmt.superclass != null) {
                    val superclass = lookUpVariableType(stmt.superclass.name, stmt.superclass) as Type.InstanceType
//                    environment = Environment(environment)

                    environment.define("super", superclass)
                }

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, typeParameters, emptyList(), members, stmt.superclass))

                stmt.fields.forEach {
                    typeCheck(it)
                    members[it.name.lexeme] = it.type
                }


                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, typeParameters, emptyList(), members, stmt.superclass))

                stmt.constructor.parameters.forEachIndexed {index, pair ->
                    params.add(pair.second)
                    if(stmt.constructor.fields.containsKey(index)) {
                        members[pair.first.lexeme] = pair.second
                    }
                }

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, typeParameters, emptyList(), members, stmt.superclass))

                stmt.methods.forEach {
                    typeCheck(it)
                    members[it.name.lexeme] = it.functionBody.type
                }


                typeCheck(stmt.constructor)

                environment.define(stmt.name.lexeme, Type.InstanceType(Expr.Variable(stmt.name), params, typeParameters, emptyList(), members, stmt.superclass))

                environment.define("this", Type.InstanceType(Expr.Variable(stmt.name), params, typeParameters, emptyList(), members, stmt.superclass))
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
            is Type.UnresolvedType -> {
                val resolvedType = lookUpVariableType(type.identifier.name, type.identifier)
                if (resolvedType is Type.InstanceType) {
                    if(type.typeArguments.size == resolvedType.typeParams.size) {
                        val args = mutableMapOf<String, Type>()
                        resolvedType.typeParams.forEachIndexed { index, unresolvedType ->
                            args[unresolvedType.identifier.name.lexeme] = type.typeArguments[index]
                        }
                        resolveTypeArgument(args, resolvedType)
                    } else resolvedType
                } else resolvedType
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

    fun resolveTypeArgument(args: Map<String, Type>, type: Type): Type {
        return when (type) {
            is Type.UnresolvedType -> {
                if (args.containsKey(type.identifier.name.lexeme)) args[type.identifier.name.lexeme] ?: type
                else type
            }
            is Type.IntegerType -> type
            is Type.FunctionType -> Type.FunctionType(
                type.params.map { resolveTypeArgument(args, it) },
                type.typeParams,
                resolveTypeArgument(args, type.result)
            )
            is Type.InstanceType -> {
                Type.InstanceType(
                    type.className,
                    type.params.map { resolveTypeArgument(args, it) },
                    type.typeParams, type.typeArguments,
                    type.members.map {
                        Pair(it.key, resolveTypeArgument(args, it.value))

                    }.toMap(),
                    type.superclass
                )
            }
            is Type.ArrayType -> Type.ArrayType(resolveTypeArgument(args, type.type))
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
                    val calleeType = resolveInstanceType(lookUpVariableType(className, expr.obj)) as Type.InstanceType
                    val thisType = resolveInstanceType(calleeType.getMemberType(expr.name.lexeme, this))
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

                    val arguments = expr.arguments
                    val params = calleeType.params.map{resolveInstanceType(it)}
                    val typeParams = calleeType.typeParams
                    val typeArguments = inferTypeArguments(expr.typeArguments, typeParams, params, expr.arguments)

                    typeCheckArguments(params, arguments)

                    typeParams.forEachIndexed { index, typeParam ->
                        environment.define(typeParam.identifier.name.lexeme, typeArguments[index])
                    }

                    val resultType = calleeType.result

                    checkGenericCall(arguments, params)

                    val thisType = if (resultType is Type.UnresolvedType) {
                     lookUpVariableType(resultType.identifier.name, resultType.identifier)

                    } else {
                        resultType
                    }
                    expr.type = thisType

                    thisType
                } else if (calleeType is Type.InstanceType) {

                    val typeParams = calleeType.typeParams

                    val instanceType = lookUpVariableType(calleeType.className.name, calleeType.className) as Type.InstanceType

                    val params = calleeType.params

                    val typeArguments = inferTypeArguments(expr.typeArguments, typeParams, params, expr.arguments)



                    val args = typeParams.zip(typeArguments).associate {
                        Pair(it.first.identifier.name.lexeme, it.second)
                    }.toMap()

                    val modifiedClass = resolveTypeArgument(args, instanceType)
                    typeCheckArguments((modifiedClass as Type.InstanceType).params, expr.arguments)


                    checkGenericCall(expr.arguments, (modifiedClass).params)

                    expr.type = modifiedClass
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

                val typeParameters = expr.typeParameters.map {
                    Type.UnresolvedType(Expr.Variable(it), emptyList())

                }

                typeParameters.forEach {
                    environment.define(it.identifier.name.lexeme, it)
                }
                val closure = Environment(environment)


                expr.parameters.forEachIndexed {index, token ->
                    var parameterType = (expr.type as Type.FunctionType).params[index]
                    if (parameterType is Type.InstanceType && expr.typeParameters.any { it.lexeme == parameterType.className.name.lexeme}) {
                        closure.define(token.lexeme, Type.InferrableType())
                    } else {

                        closure.define(token.lexeme, resolveInstanceType(parameterType))
                    }
                }
                val returnType = getBlockType(block.statements, closure)
                if ((expr.type as Type.FunctionType).result is Type.InferrableType) {
                    expr.type = Type.FunctionType((expr.type as Type.FunctionType).params, typeParameters, returnType)
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
                expr.conditions = expr.conditions.map {
                    Pair(resolveInstanceType(it.first), it.second)
                }

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
                if (!isExhuastive(expr.variable.type, expr.conditions.map { resolveInstanceType(it.first) }, hasElse)) {
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

    private fun inferTypeArguments(
        typeArguments: List<Type>,
        typeParams: List<Type.UnresolvedType>,
        params: List<Type>,
        arguments: List<Expr>
    ): List<Type> {
        if (typeArguments.size != typeParams.size) {
            if (typeArguments.isEmpty()) {
                val typeArgMap = mutableMapOf<String, Type>()
                val newTypeArgs = mutableListOf<Type>()
                params.forEachIndexed { index, it ->
                    if (it is Type.UnresolvedType) {
                        if (typeParams.any { param -> it.identifier.name.lexeme == param.identifier.name.lexeme }) {
                            typeArgMap[it.identifier.name.lexeme] = arguments[index].type
                        }
                    }
                }
                typeParams.forEach {
                    if (typeArgMap.containsKey(it.identifier.name.lexeme))
                        typeArgMap[it.identifier.name.lexeme]?.let { it1 -> newTypeArgs.add(it1) }
                }
                return newTypeArgs
            } else {
                typeErrors.add("Number of type params and type arguments don't match")
                return emptyList<Type>()
            }
        } else {
            return typeArguments
        }
    }

    private fun checkGenericCall(arguments: List<Expr>, params: List<Type>) {
        arguments.forEachIndexed { index, arg ->
            val paramType = params[index]
            if (paramType is Type.UnresolvedType) {
                val expectedType = lookUpVariableType(paramType.identifier.name, paramType.identifier)
                if (!arg.type.canAssignTo(expectedType, this)) {
                    typeErrors.add("Expected $expectedType and got ${arg.type}")
                }
            }
        }
    }

    private fun typeCheckArguments(
        params: List<Type>,
        arguments: List<Expr>
    ) {
        params.forEachIndexed { index, param ->
            val argType = arguments[index].type
            if (!param.canAssignTo(argType, this)) {
                typeErrors.add("Expected $param and got $argType")
            }
        }
    }

    private fun isExhuastive(type: Type, branches: List<Type>, hasElse: Boolean): Boolean {
        if (hasElse) return true
        else {
            if (type is Type.AnyType) return false
            return if (type is Type.UnionType) {
                type.types.all {type -> branches.any {branch -> type.canAssignTo(resolveInstanceType(branch), this) } }
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
package frontend.analysis

import frontend.*

class TypeChecker(val locals: MutableMap<Expr, Int>) {

    val globals = TypeScope()
    var environment = globals

    val typeErrors: MutableList<String> = mutableListOf()

    fun typeCheck(statements: List<Stmt>) {
        statements.forEach { checkDeclarations(it) }
        statements.forEach { typeCheck(it) }
    }

    fun checkDeclarations(stmt: Stmt) {
        when (stmt) {
            is Stmt.Class -> {
                val typeParameters = stmt.constructor.typeParameters.map {
                    Type.UnresolvedType(
                        Expr.Variable(it),
                        emptyList()
                    )
                }

                val memberMap = mutableMapOf<String, Type>()
                val params = mutableListOf<Type>()
                stmt.fields.forEach {
                    memberMap[it.name.lexeme] = it.type
                }

                stmt.methods.forEach {
                    checkDeclarations(it.function)
                    memberMap[it.function.name.lexeme] = it.function.functionBody.type
                }

                checkDeclarations(stmt.constructor)

                stmt.constructor.parameters.forEachIndexed { index, pair ->
                    params.add(pair.second)
                    if (stmt.constructor.fields.containsKey(index)) {
                        memberMap[pair.first.lexeme] = pair.second
                    }
                }

                val instance = Type.InstanceType(
                    Expr.Variable(stmt.name),
                    params,
                    typeParameters,
                    emptyList(),
                    memberMap,
                    null,
                    emptyList()
                )

                environment.defineValue(
                    stmt.name.lexeme,
                    instance
                )
            }
            is Stmt.ClassDeclaration -> typeCheckClassDeclaration(stmt)
            is Stmt.Constructor -> {
            }
            is Stmt.Expression -> checkDeclarations(stmt.expression)
            is Stmt.Function -> {
                environment.defineValue(stmt.name.lexeme, stmt.functionBody.type)
                checkDeclarations(stmt.functionBody)
            }
            is Stmt.ConstructorDeclaration -> typeCheckConstructorDeclaration(stmt)
            is Stmt.FunctionDeclaration -> {
                val declaration = stmt.type as Type.FunctionType
                environment.defineValue(stmt.name.lexeme, stmt.type)
            }
            is Stmt.If -> {
                checkDeclarations(stmt.thenBranch)
                stmt.elseBranch?.let { checkDeclarations(it) }
            }
            is Stmt.Interface -> {
                val members = mutableMapOf<String, Type>()
                stmt.methods.forEach {
                    members[it.name.lexeme] = it.type
                }
                stmt.fields.forEach {
                    members[it.name.lexeme] = it.type
                }
//                environment.defineValue(stmt.name.lexeme, Type.InterfaceType(members))
                environment.defineType(stmt.name.lexeme, Type.InterfaceType(members))
            }
            is Stmt.Print -> {
            }
            is Stmt.PrintType -> {
            }
            is Stmt.Var -> {
            }
            is Stmt.VarDeclaration -> {
            }
            is Stmt.While -> {
                checkDeclarations(stmt.body)
            }
            is Stmt.Enum -> {
                val container = Type.EnumContainer(stmt.name, stmt.members)
                environment.defineValue(stmt.name.lexeme, container)
                environment.defineType(stmt.name.lexeme, Type.EnumType(container))
            }
            is Stmt.Destructure -> {
            }
            is Stmt.TypeDeclaration -> {
                environment.defineType(stmt.name.lexeme, stmt.type)
            }
        }
    }

    fun checkDeclarations(expr: Expr) {
        when (expr) {
            is Expr.Array -> expr.values.forEach { typeCheck(it) }
            is Expr.Assign -> {
            }
            is Expr.Binary -> {
            }
            is Expr.Block -> {
                val previous = this.environment

                this.environment = TypeScope(environment)

                expr.statements.forEach { checkDeclarations(it) }
                this.environment = previous
            }
            is Expr.Call -> {
            }
            is Expr.Function -> {
            }
            is Expr.Get -> {
            }
            is Expr.Grouping -> {
            }
            is Expr.If -> {
                checkDeclarations(expr.condition)
                checkDeclarations(expr.thenBranch)
                checkDeclarations(expr.elseBranch)
            }
            is Expr.Literal -> {
            }
            is Expr.Logical -> {
            }
            is Expr.Match -> {
            }
            is Expr.Set -> {
            }
            is Expr.Super -> {
            }
            is Expr.This -> {
            }
            is Expr.TypeMatch -> {
            }
            is Expr.Unary -> checkDeclarations(expr.right)
            is Expr.Variable -> {
            }
        }
    }

    fun typeCheck(stmt: Stmt) {
        when (stmt) {
            is Stmt.Class -> typeCheckClass(stmt)
            is Stmt.Constructor -> typeCheckConstructor(stmt)
            is Stmt.ConstructorDeclaration, is Stmt.ClassDeclaration, is Stmt.FunctionDeclaration -> { }
            is Stmt.Expression -> typeCheck(stmt.expression)
            is Stmt.Function -> {
                environment.defineValue(stmt.name.lexeme, stmt.functionBody.type)
                val type = typeCheck(stmt.functionBody)
                environment.defineValue(stmt.name.lexeme, type)
            }
            is Stmt.If -> {
                typeCheck(stmt.condition)
                typeCheck(stmt.thenBranch)
                if (!isBooleanType(stmt.condition.type)) {
                    typeErrors.add("If condition should be boolean")
                }
                stmt.elseBranch?.let { typeCheck(it) }
            }
            is Stmt.Print -> typeCheck(stmt.expression)
            is Stmt.PrintType -> typeCheck(stmt.expression)
            is Stmt.Var -> {
                val initialiserType = lookupInitialiserType(stmt.type)
                val assignedType = typeCheck(stmt.initializer)
                val type = if (stmt.type is Type.InferrableType)
                    assignedType else resolveInstanceType(initialiserType)
                val canAssign = initialiserType.canAssignTo(assignedType, this)

                if (!canAssign) {
                    typeErrors.add("Cannot assign ${assignedType} to $initialiserType")
                }
                stmt.type = type
                environment.defineValue(stmt.name.lexeme, type)
            }
            is Stmt.While -> {
                typeCheck(stmt.condition)

                if (!isBooleanType(stmt.condition.type)) {
                    typeErrors.add("While condition should be boolean")
                }
                typeCheck(stmt.body)
            }
            is Stmt.Interface -> {
            }
            is Stmt.VarDeclaration -> {
            }

            is Stmt.Destructure -> {
                val assignedType = typeCheck(stmt.initializer)
                if (assignedType is Type.TupleType) {

                    stmt.names.forEachIndexed { index, varDeclaration ->
                        val sliceType = assignedType.types[index]
                        val declaredType = if (varDeclaration.type is Type.InferrableType)
                            sliceType else resolveInstanceType(varDeclaration.type)
                        val canAssign = declaredType.canAssignTo(sliceType, this)

                        if (!canAssign) {
                            typeErrors.add("Cannot assign ${sliceType} to $declaredType")
                        }

                        environment.defineValue(varDeclaration.name.lexeme, declaredType)

                    }
                } else {
                    typeErrors.add("Can only destructure tuples")
                }
            }
        }
    }

    private fun typeCheckConstructorDeclaration(stmt: Stmt.ConstructorDeclaration) {
        stmt.parameters.forEachIndexed { index, pair ->
            if (stmt.fields.containsKey(index)) {
                environment.defineValue(pair.first.lexeme, pair.second)
            }
        }

        stmt.parameters.forEach { pair ->
            environment.defineValue(pair.first.lexeme, pair.second)
        }
    }

    private fun typeCheckConstructor(stmt: Stmt.Constructor) {
        stmt.parameters.forEachIndexed { index, pair ->
            if (stmt.fields.containsKey(index)) {
                environment.defineValue(pair.first.lexeme, pair.second)
            }
        }

        stmt.parameters.forEach { pair -> environment.defineValue(pair.first.lexeme, pair.second) }
        getBlockType(stmt.constructorBody.statements, environment)
    }

    private fun typeCheckClassDeclaration(stmt: Stmt.ClassDeclaration) {
        val typeParameters = stmt.constructor.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        typeParameters.forEach { environment.defineValue(it.identifier.name.lexeme, it) }

        val memberMap = mutableMapOf<String, Type>()
        val params = mutableListOf<Type>()
        stmt.fields.forEach {
            memberMap[it.name.lexeme] = it.type
        }

        stmt.methods.forEach {
            checkDeclarations(it)
            memberMap[it.name.lexeme] = it.type
        }

        checkDeclarations(stmt.constructor)

        stmt.constructor.parameters.forEachIndexed { index, pair ->
            params.add(pair.second)
            if (stmt.constructor.fields.containsKey(index)) {
                memberMap[pair.first.lexeme] = pair.second
            }
        }

        environment.defineValue(
            stmt.name.lexeme, Type.InstanceType(
                Expr.Variable(stmt.name),
                params,
                typeParameters,
                emptyList(),
                memberMap,
                null,
                emptyList()
            )
        )
    }

    private fun typeCheckClass(stmt: Stmt.Class) {
        val previous = this.environment
        this.environment = TypeScope(this.environment)

        val referencedInstance = environment.getValue(stmt.name) as Type.InstanceType
        this.environment.defineValue("this", referencedInstance)

        val members = mutableMapOf<String, Type>()
        val params = mutableListOf<Type>()

        val typeParameters = stmt.constructor.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        typeParameters.forEach {
            environment.defineValue(it.identifier.name.lexeme, it)
        }

        val superclass =
            if (stmt.superclass != null) lookUpVariableType(stmt.superclass.name, stmt.superclass) as Type.InstanceType
            else null

        val superTypeArgs = stmt.constructor.superTypeArgs

        if (superclass != null) {
            environment.defineValue("super", superclass)
        }

        typeCheck(stmt.constructor)

        stmt.fields.forEach {
            typeCheck(it)
            members[it.name.lexeme] = it.type
            environment.defineValue(it.name.lexeme, it.type)
        }

        stmt.constructor.parameters.forEachIndexed { index, pair ->
            params.add(pair.second)
            if (stmt.constructor.fields.containsKey(index)) {
                members[pair.first.lexeme] = pair.second
                environment.defineValue(pair.first.lexeme, pair.second)
            }
        }

        stmt.methods.forEach {
            typeCheck(it.function)
            members[it.function.name.lexeme] = it.function.functionBody.type
            environment.defineValue(it.function.name.lexeme, it.function.functionBody.type)
        }

        val instance = Type.InstanceType(
            Expr.Variable(stmt.name),
            params, typeParameters,
            emptyList(), members,
            superclass, superTypeArgs
        )

        environment.defineValue(
            "this",
            instance
        )

        this.environment = previous

        environment.defineValue(
            stmt.name.lexeme,
            instance
        )

        stmt.interfaces.forEach {
            val referencedInterface = resolveInstanceType(environment.getType(it) as Type.InterfaceType) as Type.InterfaceType

            referencedInterface.members.entries.forEach {  }
            if (!referencedInterface.canAssignTo(instance, this)) {
                typeErrors.add("Cannot assign ${stmt.name.lexeme} to ${it.lexeme}")
                val missing = referencedInterface.members.filter { !instance.hasMemberType(it.key, it.value, this) }
                missing.entries.forEach {
                    typeErrors.add("${stmt.name.lexeme} is missing ${it.key}, ${it.value}")
                }
            }

        }
    }


    private fun lookupInitialiserType(type: Type): Type = when (type) {
        is Type.UnresolvedType -> {
            if (!typeExists(type.identifier.name, type.identifier))  resolveInstanceType(type)
            else lookUpType(type.identifier.name, type.identifier)
        }
        else -> type
    }

    private fun resolveInstanceType(type: Type): Type {
        return when (type) {
            is Type.UnresolvedType -> {
                if (!exists(type.identifier.name, type.identifier)) {
                    return type
                }
                val resolvedType = lookUpVariableType(type.identifier.name, type.identifier)
                if (resolvedType is Type.InstanceType) {
                    if (type.typeArguments.size == resolvedType.typeParams.size) {
                        val args = mutableMapOf<String, Type>()
                        resolvedType.typeParams.forEachIndexed { index, unresolvedType ->
                            args[unresolvedType.identifier.name.lexeme] = resolveInstanceType(type.typeArguments[index])
                        }
                        resolveTypeArgument(args, resolvedType)
                    } else resolvedType
                } else resolvedType
            }
            is Type.UnionType -> Type.UnionType(type.types.map { resolveInstanceType(it) })
            is Type.InstanceType -> {
                val newMembers = type.members.entries.associate {
                    val newType = resolveInstanceType(it.value)
                    it.key to newType
                }
                return type.copy(members = newMembers)
            }
            is Type.InterfaceType -> {
                val newMembers = type.members.entries.associate {
                    val newType = resolveInstanceType(it.value)
                    it.key to newType
                }
                return Type.InterfaceType(members = newMembers)
            }
            is Type.FunctionType -> {
                val result = resolveInstanceType(type.result)
                val params = resolveInstanceType(type.params) as Type.TupleType
                return Type.FunctionType(params, type.typeParams, result)
            }
            is Type.TupleType -> {
                return Type.TupleType(type.types.map { resolveInstanceType(it) })
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
            is Type.TupleType -> Type.TupleType(type.types.map { resolveTypeArgument(args, it) })
            is Type.FunctionType -> Type.FunctionType(
                resolveTypeArgument(args, type.params) as Type.TupleType,
                type.typeParams,
                resolveTypeArgument(args, type.result)
            )
            is Type.InstanceType -> {
                val superClass = type.superclass
                val superArgs: MutableMap<String, Type> = superClass?.typeParams?.zip(type.superTypeArgs)?.associate {
                    var paramType = it.second
                    if (paramType is Type.UnresolvedType) {
                        if (args.containsKey(paramType.identifier.name.lexeme))
                            paramType = args[paramType.identifier.name.lexeme]!!
                    }
                    Pair(it.first.identifier.name.lexeme, paramType)
                }?.toMutableMap()
                    ?: mutableMapOf()

                val typeArguments = type.typeParams.map {
                    args[it.identifier.name.lexeme]
                }.filterNotNull()
                Type.InstanceType(
                    type.className,
                    type.params.map { resolveTypeArgument(args, it) },
                    type.typeParams, typeArguments,
                    type.members.map {
                        Pair(it.key, resolveTypeArgument(args, it.value))

                    }.toMap(),
                    if (superClass != null) resolveTypeArgument(superArgs, superClass) as Type.InstanceType else null,
                    type.superTypeArgs.map { resolveTypeArgument(args, it) }
                )
            }
            else -> type
        }
    }

    fun typeCheck(expr: Expr): Type {
        return when (expr) {
            is Expr.Get -> typeCheckGet(expr)
            is Expr.Array -> {
                val elementTypes = expr.values.map { typeCheck(it) }
                val type = flattenTypes(elementTypes)
                val thisType = createArrayType(type)
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
            is Expr.Binary -> typeCheckBinary(expr)
            is Expr.Block -> {
                val thisType = getBlockType(expr.statements, TypeScope(environment))
                expr.type = thisType
                thisType
            }
            is Expr.Call -> typeCheckCall(expr)
            is Expr.Function -> typeCheckFunction(expr)
            is Expr.Grouping -> {
                expr.type = typeCheck(expr.expr)
                expr.type
            }
            is Expr.If -> {
                typeCheck(expr.condition)
                if (!isBooleanType(expr.condition.type)) {
                    typeErrors.add("If condition should be boolean")
                }
                val thenType = typeCheck(expr.thenBranch)
                val elseType = typeCheck(expr.elseBranch)
                val thisType =
                    if (thenType::class == elseType::class) thenType else Type.UnionType(listOf(thenType, elseType))
                expr.type = thisType
                thisType
            }
            is Expr.Literal -> {
                val type = when (expr.value) {
                    is String -> createStringType()
                    is Double -> createDecimalType()
                    is Int -> createIntegerType()
                    is Boolean -> createBooleanType()
                    is Char -> createCharType()
                    else -> Type.NullType()
                }
                expr.type = type
                expr.type
            }
            is Expr.Logical -> {
                val left = typeCheck(expr.left)
                val right = typeCheck(expr.right)
                if (!isBooleanType(left) || !isBooleanType(right)) {
                    typeErrors.add("Logical expression should have boolean left and right")
                }
                val thisType = createBooleanType()
                expr.type = thisType
                thisType
            }
            is Expr.Set -> typeCheckSet(expr)
            is Expr.Super -> {
                val distance = locals[expr]
                val superclass = distance?.let { environment.getValueAt(it - 1, "super") } as Type.InstanceType
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
                val operandType = typeCheck(expr.right)
                if (operandType is Type.InstanceType) {
                    val returnType = operandType.getUnaryOperatorType(expr.operator.type, this)
                    if (returnType != null) {
                        val resolved = resolveInstanceType(returnType)
                        expr.type = resolved
                        resolved
                    } else {
                        expr.type = operandType
                        operandType
                    }
                } else {
                    expr.type = operandType
                    operandType
                }
            }
            is Expr.Variable -> {
                val type = lookUpVariableType(expr.name, expr)
                expr.type = type
                type
            }
            is Expr.TypeMatch -> getTypeMatchType(expr)
            is Expr.Match -> getMatchType(expr)
            is Expr.Tuple -> {
                val elementTypes = expr.values.map { typeCheck(it) }
                val thisType = Type.TupleType(elementTypes)
                expr.type = thisType
                return thisType
            }
        }
    }

    private fun typeCheckBinary(expr: Expr.Binary): Type {
        val left = typeCheck(expr.left)
        val right = typeCheck(expr.right)

        if (left is Type.InstanceType) {
            val returnType = left.getBinaryOperatorType(expr.operator.type, right, this)
            val resolved = if (returnType is Type.UnresolvedType) {
                resolveInstanceType(returnType)
            } else returnType
            if (resolved != null) {
                expr.type = resolved
                return resolved
            }
        } else {
            typeErrors.add("Cannot call ${expr.operator.type} on $left")
        }

        expr.type = Type.NullType()
        return expr.type
    }

    private fun typeCheckCall(expr: Expr.Call): Type {
        val calleeType = typeCheck(expr.callee)
        expr.arguments.forEach { typeCheck(it) }

        return when (calleeType) {
            is Type.FunctionType -> typeCheckFunctionCall(expr, calleeType)
            is Type.InstanceType -> typeCheckInstanceCall(calleeType, expr)
            else -> calleeType
        }
    }

    private fun typeCheckInstanceCall(calleeType: Type.InstanceType, expr: Expr.Call): Type {
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
        return expr.type
    }

    private fun typeCheckFunctionCall(expr: Expr.Call, calleeType: Type.FunctionType): Type {
        val arguments = expr.arguments
        val params = (resolveInstanceType(calleeType.params) as Type.TupleType).types
        val typeParams = calleeType.typeParams
        val typeArguments = inferTypeArguments(expr.typeArguments, typeParams, params, expr.arguments)

        typeCheckArguments(params, arguments)

        typeParams.forEachIndexed { index, typeParam ->
            environment.defineValue(typeParam.identifier.name.lexeme, typeArguments[index])
        }

        val resultType = calleeType.result

        checkGenericCall(arguments, params)

        val thisType = resolveInstanceType(resultType)
        expr.type = thisType

        return thisType
    }

    private fun typeCheckGet(expr: Expr.Get): Type {
        typeCheck(expr.obj)
        return if (isArrayType(expr.obj.type) && expr.index != null) {
            val thisType = (expr.obj.type as Type.InstanceType).typeArguments[0]
            typeCheck(expr.index)
            if (!isIntegerType(expr.index.type)) {
                typeErrors.add("Array index should be an integer")
            }

            expr.type = thisType
            thisType
        } else {
            val calleeType = typeCheck(expr.obj)
            val thisType = calleeType.getMemberType(expr.name.lexeme, this)
            val resolved = if (thisType is Type.UnresolvedType) resolveInstanceType(thisType)
            else thisType
            expr.type = resolved
            resolved
        }
    }

    private fun typeCheckSet(expr: Expr.Set): Type {
        typeCheck(expr.obj)
        val left = expr.obj.type
        return if (isArrayType(left)) {
            typeCheck(expr.value)
            val arrType = (left as Type.InstanceType).typeArguments[0]
            if (expr.index != null) {
                typeCheck(expr.index)
                if (!isIntegerType(expr.index.type)) {
                    typeErrors.add("Array index should be integer")
                }
            }
            if (!arrType.canAssignTo(expr.value.type, this)) {
                typeErrors.add("Cannot assign ${expr.value.type} to ${arrType}")
            }
            val thisType = expr.value.type
            thisType
        } else {
            val field = left.getMemberType(expr.name.lexeme, this)
            typeCheck(expr.value)
            if (!field.canAssignTo(expr.value.type, this)) {
                typeErrors.add("Cannot assign ${expr.value.type} to $field")
            }
            val thisType = expr.value.type
            thisType
        }
    }

    private fun getMatchType(expr: Expr.Match): Type {
        typeCheck(expr.expr)
        if (!isIntegerType(expr.expr.type) && !isDecimalType(expr.expr.type) && !isStringType(expr.expr.type)  && expr.expr.type !is Type.EnumType) {
            typeErrors.add("Can only use integer, double, enum, or string types in match")
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
        return expr.type
    }

    private fun getTypeMatchType(expr: Expr.TypeMatch): Type {
        val variableType = typeCheck(expr.variable)
        if (expr.variable.type !is Type.AnyType && expr.variable.type !is Type.UnionType) {
            typeErrors.add("Can only type match any and union types")
        }
        val types = mutableListOf<Type>()
        expr.conditions = expr.conditions.map {
            Triple(resolveInstanceType(it.first), it.second, it.third)
        }

        expr.conditions.forEach {
            val block: Expr.Block = makeBlock(it.second)

            val closure = TypeScope(environment)
            val alias = it.third
            if (alias != null) {
                closure.defineValue(alias.lexeme, it.first)
                closure.defineValue(expr.variable.name.lexeme, variableType)
            } else {
                closure.defineValue(expr.variable.name.lexeme, it.first)
            }

            val returnType = getBlockType(block.statements, closure)
            types.add(returnType)
        }

        if (expr.elseBranch != null) {
            typeCheck(expr.elseBranch)
            types.add(expr.elseBranch.type)
        }

        val hasElse = expr.elseBranch != null
        if (!isExhuastive(expr.variable.type, expr.conditions.map { resolveInstanceType(it.first) }, hasElse)) {
            typeErrors.add("Typematch is not exhuastive")
        }
        expr.type = flattenTypes(types)
        return expr.type
    }

    private fun typeCheckFunction(expr: Expr.Function): Type {
        val block = makeBlock(expr.body)

        val typeParameters = expr.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        typeParameters.forEach {
            environment.defineValue(it.identifier.name.lexeme, it)
        }
        val closure = TypeScope(environment)

        expr.parameters.forEachIndexed { index, token ->
            val parameterType = (expr.type as Type.FunctionType).params.types[index]
            if (parameterType is Type.InstanceType && expr.typeParameters.any { it.lexeme == parameterType.className.name.lexeme }) {
                closure.defineValue(token.lexeme, Type.InferrableType())
            } else {
                closure.defineValue(token.lexeme, lookupInitialiserType(parameterType))
            }
        }
        val blockReturnType = getBlockType(block.statements, closure)
        val definition = (expr.type as Type.FunctionType)
        val paramTypes = (expr.type as Type.FunctionType).params.types.map { lookupInitialiserType(it) }

        val returnType = if (definition.result is Type.InferrableType) blockReturnType else definition.result

        expr.type = Type.FunctionType(Type.TupleType(paramTypes), typeParameters, returnType)

        return expr.type
    }

    private fun makeBlock(expr: Expr) = if (expr is Expr.Block)
        expr
    else
        Expr.Block(listOf(Stmt.Expression(expr)))

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
                return emptyList()
            }
        } else {
            return typeArguments
        }
    }

    private fun checkGenericCall(arguments: List<Expr>, params: List<Type>) {
        arguments.forEachIndexed { index, arg ->
            val paramType = params[index]
            if (paramType is Type.UnresolvedType) {

                var expectedType = resolveInstanceType(paramType)
                if (expectedType is Type.UnresolvedType) {
                    if (exists(expectedType.identifier.name, expectedType.identifier)) {
                        expectedType = resolveInstanceType(expectedType)
                    }
                }
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
        return if (hasElse) true
        else {
            if (type is Type.AnyType) return false
            if (type is Type.UnionType) type.types.all {
                branches.any { branch ->
                    it.canAssignTo(
                        resolveInstanceType(branch),
                        this
                    )
                }
            } else false
        }
    }

    private fun isArrayType(type: Type): Boolean =
        type is Type.InstanceType && type.className.name.lexeme == "Array"

    fun createArrayType(type: Type): Type = resolveInstanceType(
        Type.UnresolvedType(
            Expr.Variable(Token(TokenType.IDENTIFIER, "Array", null, -1)),
            listOf(type)
        )
    )

    fun createIntegerType() =
        lookUpVariableType(
            Token(TokenType.IDENTIFIER, "Int", null, -1),
            Expr.Literal(2)
        )

    fun createDecimalType() =
        lookUpVariableType(
            Token(TokenType.IDENTIFIER, "Decimal", null, -1),
            Expr.Literal(2)
        )

    fun createStringType() =
        lookUpVariableType(
            Token(TokenType.IDENTIFIER, "String", null, -1),
            Expr.Literal(2)
        )

    fun createBooleanType() =
        lookUpVariableType(
            Token(TokenType.IDENTIFIER, "Boolean", null, -1),
            Expr.Literal(2)
        )

    fun createCharType() =
        lookUpVariableType(
            Token(TokenType.IDENTIFIER, "Char", null, -1),
            Expr.Literal(2)
        )

    fun isIntegerType(type: Type): Boolean = when (type) {
        is Type.InstanceType -> type.className.name.lexeme == "Int"
        is Type.UnresolvedType -> isIntegerType(resolveInstanceType(type))
        else -> false
    }

    fun isDecimalType(type: Type): Boolean = when (type) {
        is Type.InstanceType -> type.className.name.lexeme == "Decimal"
        is Type.UnresolvedType -> isDecimalType(resolveInstanceType(type))
        else -> false
    }

    fun isStringType(type: Type): Boolean = when (type) {
        is Type.InstanceType -> type.className.name.lexeme == "String"
        is Type.UnresolvedType -> isStringType(resolveInstanceType(type))
        else -> false
    }

    fun isBooleanType(type: Type): Boolean = when (type) {
        is Type.InstanceType -> type.className.name.lexeme == "Boolean"
        is Type.UnresolvedType -> isBooleanType(resolveInstanceType(type))
        else -> false
    }


    fun flattenTypes(elementTypes: List<Type>): Type {
        val instanceTypes = elementTypes.filterIsInstance<Type.InstanceType>().toSet().toList()
        val nonInstanceTypes = elementTypes.filter { it !is Type.InstanceType }.distinctBy { it::class }


        val resultTypes = nonInstanceTypes + instanceTypes
        return if (resultTypes.size == 1) resultTypes[0] else Type.UnionType(resultTypes)
    }

    fun exists(name: Token, expr: Expr): Boolean {
        val distance = locals[expr]
        return if (distance != null)
            environment.getValueAt(distance, name.lexeme) != null
        else
            globals.getValue(name) != null
    }

    fun typeExists(name: Token, expr: Expr): Boolean {
        return environment.getType(name) != null
    }

    fun lookUpVariableType(name: Token, expr: Expr): Type {
        val distance = locals[expr]
        return if (distance != null)
            environment.getValueAt(distance, name.lexeme) as Type
        else
            globals.getValue(name) as Type
    }

    fun lookUpType(name: Token, expr: Expr): Type {
        return environment.getType(name) as Type
    }

    fun getBlockType(statements: List<Stmt>, environment: TypeScope): Type {
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
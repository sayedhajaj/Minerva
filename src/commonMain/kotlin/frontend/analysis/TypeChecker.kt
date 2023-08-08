package frontend.analysis

import frontend.*

val arithmeticOperators = listOf(
    TokenType.PLUS,
    TokenType.MINUS,
    TokenType.SLASH,
    TokenType.STAR,
    TokenType.MODULO
)

val operatorMethods = mapOf(
    TokenType.PLUS to "add",
    TokenType.MINUS to "subtract",
    TokenType.SLASH to "divide",
    TokenType.STAR to "multiply",
    TokenType.MODULO to "rem"
)

class TypeChecker(override var locals: MutableMap<Expr, Int>) : ITypeChecker {

    val symbolTable = SymbolTable(locals)

    override val typeErrors: MutableList<CompileError.TypeError> = mutableListOf()

    fun typeCheck(statements: List<Stmt>) {
        preVisitDeclarations(statements)

        statements.forEach { typeCheck(it) }
    }

    fun typeCheck(stmt: Stmt) {
        when (stmt) {
            is Stmt.Class -> typeCheckClass(stmt)
            is Stmt.Constructor -> typeCheckConstructor(stmt)
            is Stmt.ConstructorDeclaration, is Stmt.ClassDeclaration, is Stmt.FunctionDeclaration, is Stmt.VarDeclaration -> {}
            is Stmt.Expression -> typeCheck(stmt.expression)
            is Stmt.Function -> {
                symbolTable.defineValue(stmt.name.lexeme, stmt.functionBody.type)
                val type = typeCheck(stmt.functionBody)
                symbolTable.defineValue(stmt.name.lexeme, type)
            }
            is Stmt.If -> typeCheckIfStmt(stmt)
            is Stmt.Print -> typeCheck(stmt.expression)
            is Stmt.PrintType -> typeCheck(stmt.expression)
            is Stmt.Var -> typeCheckVarStmt(stmt)
            is Stmt.While -> {
                typeCheck(stmt.condition)

                if (!isBooleanType(stmt.condition.type)) {
                    typeErrors.add(CompileError.TypeError("While condition should be boolean"))
                }
                typeCheck(stmt.body)
            }
            is Stmt.ForEach -> typeCheckForEach(stmt)
            is Stmt.Destructure -> typeCheckDestructure(stmt)
            is Stmt.Module -> typeCheckModuleStmt(stmt)
            else -> {}
        }
    }

    fun typeCheck(expr: Expr): Type {
        return when (expr) {
            is Expr.Get -> typeCheckGet(expr)
            is Expr.Array -> typeCheckArray(expr)
            is Expr.Assign -> typeCheckAssign(expr)
            is Expr.Binary -> typeCheckBinary(expr)
            is Expr.Block -> typeCheck(expr)
            is Expr.Call -> typeCheckCall(expr)
            is Expr.Function -> typeCheckFunction(expr)
            is Expr.Grouping -> typeCheck(expr)
            is Expr.If -> typeCheckIfExpr(expr)
            is Expr.Literal -> typeCheckLiteral(expr)
            is Expr.Logical -> typeCheckLogical(expr)
            is Expr.Set -> typeCheckSet(expr)
            is Expr.Super -> typeCheck(expr)
            is Expr.This -> typeCheck(expr)
            is Expr.Unary -> typeCheckUnary(expr)
            is Expr.Variable -> typeCheck(expr)
            is Expr.TypeMatch -> getTypeMatchType(expr)
            is Expr.Match -> getMatchType(expr)
            is Expr.Tuple -> typeCheck(expr)
        }
    }

    private fun typeCheck(expr: Expr.Grouping): Type {
        expr.type = typeCheck(expr.expr)
        return expr.type
    }

    private fun typeCheck(expr: Expr.Block): Type {
        symbolTable.beginScope()
        val thisType = getBlockType(expr.statements)
        expr.type = thisType
        return thisType
    }

    private fun typeCheck(expr: Expr.Variable): Type {
        val type = symbolTable.lookUpVariableType(expr.name, expr)
        expr.type = type
        return type
    }

    private fun typeCheck(expr: Expr.Tuple): Type.TupleType {
        val elementTypes = expr.values.map { typeCheck(it) }
        val thisType = Type.TupleType(elementTypes)
        expr.type = thisType
        return thisType
    }

    private fun typeCheck(expr: Expr.This): Type {
        val thisType = symbolTable.lookUpVariableType(expr.keyword, expr)
        expr.type = thisType
        return thisType
    }

    private fun typeCheck(expr: Expr.Super): Type {
        val distance = locals[expr]
        val superclass = distance?.let { symbolTable.getValueAt(it - 1, "super") } as Type.InstanceType
        val thisType = (superclass).getMemberType(expr.method.lexeme)
        expr.type = thisType
        return thisType
    }

    /*
    You can use a function or class or module when it's defined later on in the code
    To do this I have to iterate through these declarations first before checking the rest of the code
     */
    private fun preVisitDeclarations(statements: List<Stmt>) {
        statements.filterIsInstance<Stmt.ClassDeclaration>().forEach { typeCheckClassDeclaration(it) }
        statements.filterIsInstance<Stmt.Enum>().forEach {
            val container = Type.EnumContainer(it.name, it.members)
            symbolTable.defineValue(it.name.lexeme, container)
            symbolTable.defineType(it.name.lexeme, Type.EnumType(container))
        }
        statements.filterIsInstance<Stmt.ModuleDeclaration>().forEach {
            val type = getModuleDeclarationType(it)
            symbolTable.defineValue(it.name.lexeme, type)
            symbolTable.defineType(it.name.lexeme, type)
        }
        statements.filterIsInstance<Stmt.Interface>().forEach { declareInterface(it) }

        statements.filterIsInstance<Stmt.TypeDeclaration>()
            .forEach { symbolTable.defineType(it.name.lexeme, it.type) }

        statements.filterIsInstance<Stmt.FunctionDeclaration>()
            .forEach { symbolTable.defineValue(it.name.lexeme, it.type) }

        val classes = statements.filterIsInstance<Stmt.Class>()
        classes.forEach { declareClass(it) }

        val functions = statements.filterIsInstance<Stmt.Function>()
        functions.forEach { symbolTable.defineValue(it.name.lexeme, it.functionBody.type) }

        val modules = statements.filterIsInstance<Stmt.Module>()
        modules.forEach { module ->
            val type = getModuleSignature(module)
            symbolTable.defineValue(module.name.lexeme, type)
            symbolTable.defineType(module.name.lexeme, type)
        }
    }

    private fun getModuleSignature(module: Stmt.Module): Type.ModuleType {
        val moduleFields = mutableMapOf<String, Type>()

        module.classes.forEach {
            moduleFields[it.name.lexeme] = declareClassSignature(it)
        }
        module.enums.forEach {
            moduleFields[it.name.lexeme] = Type.EnumContainer(it.name, it.members)
        }
        module.functions.forEach {
            moduleFields[it.name.lexeme] = it.functionBody.type
        }
        module.fields.forEach {
            moduleFields[it.name.lexeme] = it.type
        }

        module.modules.forEach {
            moduleFields[it.name.lexeme] = getModuleSignature(it)
        }

        return Type.ModuleType(moduleFields)
    }


    private fun getModuleDeclarationType(stmt: Stmt.ModuleDeclaration): Type.ModuleType {
        val moduleFields = mutableMapOf<String, Type>()

        stmt.classes.forEach {
            moduleFields[it.name.lexeme] = getClassDeclarationType(it)
        }
        stmt.enums.forEach {
            moduleFields[it.name.lexeme] = Type.EnumContainer(it.name, it.members)
        }
        stmt.functions.forEach {
            moduleFields[it.name.lexeme] = it.type
        }
        stmt.fields.forEach {
            moduleFields[it.name.lexeme] = it.type
        }

        stmt.modules.forEach {
            moduleFields[it.name.lexeme] = getModuleDeclarationType(it)
        }


        return Type.ModuleType(moduleFields)
    }

    private fun declareInterface(stmt: Stmt.Interface) {
        val members = mutableMapOf<String, Type>()
        stmt.methods.forEach {
            members[it.name.lexeme] = it.type
        }
        stmt.fields.forEach {
            members[it.name.lexeme] = it.type
        }

        val typeParameters = stmt.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        val wrapped = wrapGeneric(Type.InterfaceType(members), typeParameters, emptyList())

        symbolTable.defineType(stmt.name.lexeme, wrapped)
    }

    private fun declareClass(stmt: Stmt.Class) {
        val instance = declareClassSignature(stmt)
        val wrappedInstance = wrapGeneric(instance, instance.typeParams, emptyList())

        symbolTable.defineValue(
            stmt.name.lexeme,
            wrapGeneric(Type.ClassType(instance.className), instance.typeParams, emptyList())
        )

        symbolTable.defineType(
            stmt.name.lexeme,
            wrappedInstance
        )
    }

    private fun declareClassSignature(stmt: Stmt.Class): Type.InstanceType {
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
            memberMap[it.function.name.lexeme] = it.function.functionBody.type
        }


        stmt.constructor.parameters.forEachIndexed { index, pair ->
            params.add(pair.second)
            if (stmt.constructor.fields.containsKey(index)) {
                memberMap[pair.first.lexeme] = pair.second
            }
        }

        return Type.InstanceType(
            Expr.Variable(stmt.name),
            params,
            typeParameters,
            emptyList(),
            memberMap,
            null,
            emptyList()
        )
    }


    private fun typeCheckModuleStmt(stmt: Stmt.Module) {
        val moduleFields = mutableMapOf<String, Type>()
        stmt.classes.forEach { declareClass(it) }

        stmt.classes.forEach {
            typeCheck(it)
            symbolTable.getValue(it.name)?.let { cls ->
                moduleFields[it.name.lexeme] = cls
            }
        }
        stmt.enums.forEach {
            typeCheck(it)
            symbolTable.getValue(it.name)?.let { e ->
                moduleFields[it.name.lexeme] = e
            }
        }
        stmt.functions.forEach {
            typeCheck(it)
            symbolTable.getValue(it.name)?.let { fn ->
                moduleFields[it.name.lexeme] = fn
            }
        }
        stmt.fields.forEach {
            typeCheck(it)
            symbolTable.getValue(it.name)?.let { field ->
                moduleFields[it.name.lexeme] = field
            }
        }

        stmt.modules.forEach {
            typeCheck(it)
            symbolTable.getValue(it.name)?.let { module ->
                moduleFields[it.name.lexeme] = module
            }
        }


        val type = Type.ModuleType(moduleFields)
        symbolTable.defineValue(stmt.name.lexeme, type)
        symbolTable.defineType(stmt.name.lexeme, type)
    }

    private fun typeCheckDestructure(stmt: Stmt.Destructure) {
        val assignedType = typeCheck(stmt.initializer)
        if (assignedType is Type.TupleType) {

            stmt.names.forEachIndexed { index, varDeclaration ->
                val sliceType = assignedType.types[index]
                val declaredType = if (varDeclaration.type is Type.InferrableType)
                    sliceType else resolveInstanceType(varDeclaration.type)
                val canAssign = declaredType.canAssignTo(sliceType)

                if (!canAssign) {
                    typeErrors.add(CompileError.TypeError("Cannot assign $sliceType to $declaredType"))
                }

                symbolTable.defineValue(varDeclaration.name.lexeme, declaredType)

            }
        } else {
            typeErrors.add(CompileError.TypeError("Can only destructure tuples"))
        }
    }

    private fun typeCheckIfStmt(stmt: Stmt.If) {
        typeCheck(stmt.condition)
        typeCheck(stmt.thenBranch)
        if (!isBooleanType(stmt.condition.type)) {
            typeErrors.add(CompileError.TypeError("If condition should be boolean"))
        }
        stmt.elseBranch?.let { typeCheck(it) }
    }

    private fun typeCheckVarStmt(stmt: Stmt.Var) {
        val initialiserType = lookupInitialiserType(stmt.type)
        val assignedType = typeCheck(stmt.initializer)
        val type = if (stmt.type is Type.InferrableType)
            assignedType else resolveInstanceType(initialiserType)
        val canAssign = initialiserType.canAssignTo(assignedType)

        if (!canAssign) {
            typeErrors.add(CompileError.TypeError("Cannot assign $assignedType to $initialiserType"))
        }
        stmt.type = type
        symbolTable.defineValue(stmt.name.lexeme, type)
    }

    private fun typeCheckForEach(stmt: Stmt.ForEach) {
        val iterableType = typeCheck(stmt.iterable)
        val iterableInterface = lookUpType(
            Token(TokenType.IDENTIFIER, "Iterable", null, -1)
        )

        if (!iterableInterface.canAssignTo(iterableType)) {
            typeErrors.add(CompileError.TypeError("$iterableType is not iterable"))
        }

        symbolTable.beginScope()

        val iterator = (iterableType.getMemberType("iterator") as Type.FunctionType).result

        val resolvedIterator = (iterator as Type.UnresolvedType).typeArguments[0]

        symbolTable.defineValue(stmt.name.lexeme, lookupInitialiserType(resolvedIterator))

        typeCheck(stmt.body)

        symbolTable.endScope()
    }


    private fun typeCheckConstructor(stmt: Stmt.Constructor) {
        stmt.parameters.forEachIndexed { index, pair ->
            if (stmt.fields.containsKey(index)) {
                symbolTable.defineValue(pair.first.lexeme, pair.second)
            }
        }

        stmt.parameters.forEach { pair -> symbolTable.defineValue(pair.first.lexeme, pair.second) }
        symbolTable.beginScope()
        getBlockType(stmt.constructorBody.statements)
    }

    private fun typeCheckClassDeclaration(stmt: Stmt.ClassDeclaration) {
        val instanceType = getClassDeclarationType(stmt)

        symbolTable.defineValue(
            stmt.name.lexeme,
            wrapGeneric(Type.ClassType(instanceType.className), instanceType.typeParams, emptyList())
        )
        symbolTable.defineType(stmt.name.lexeme, wrapGeneric(instanceType, instanceType.typeParams, emptyList()))
    }

    private fun getClassDeclarationType(stmt: Stmt.ClassDeclaration): Type.InstanceType {
        val typeParameters = stmt.constructor.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        val memberMap = mutableMapOf<String, Type>()
        val params = mutableListOf<Type>()
        stmt.fields.forEach {
            memberMap[it.name.lexeme] = it.type
        }

        stmt.methods.forEach {
            memberMap[it.name.lexeme] = it.type
        }

        stmt.constructor.parameters.forEachIndexed { index, pair ->
            params.add(pair.second)
            if (stmt.constructor.fields.containsKey(index)) {
                memberMap[pair.first.lexeme] = pair.second
            }
        }

        return Type.InstanceType(
            Expr.Variable(stmt.name),
            params,
            typeParameters,
            emptyList(),
            memberMap,
            null,
            emptyList()
        )
    }

    override fun lookupInstance(name: Token): Type.InstanceType {
        val referencedInstance = symbolTable.getType(name)
        return if (referencedInstance is Type.GenericType) referencedInstance.bodyType as Type.InstanceType
        else referencedInstance as Type.InstanceType
    }

    private fun typeCheckClass(stmt: Stmt.Class) {
        symbolTable.beginScope()

        val referencedInstance = lookupInstance(stmt.name)
        symbolTable.defineValue("this", referencedInstance)

        val members = mutableMapOf<String, Type>()
        val params = mutableListOf<Type>()

        val typeParameters = stmt.constructor.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        typeParameters.forEach {
            symbolTable.defineType(it.identifier.name.lexeme, it)
        }

        val superclass =
            if (stmt.superclass != null) lookUpType(stmt.superclass.name) as Type.InstanceType
            else null

        val superTypeArgs = stmt.constructor.superTypeArgs

        if (superclass != null) {
            symbolTable.defineValue("super", superclass)
        }

        typeCheck(stmt.constructor)

        stmt.constructor.parameters.forEachIndexed { index, pair ->
            params.add(pair.second)
            symbolTable.defineValue(pair.first.lexeme, resolveInstanceType(pair.second))
            if (stmt.constructor.fields.containsKey(index)) {
                members[pair.first.lexeme] = pair.second

            }
        }

        stmt.fields.forEach {
            typeCheck(it)
            members[it.name.lexeme] = it.type
            symbolTable.defineValue(it.name.lexeme, it.type)
        }

        stmt.methods.forEach {
            typeCheck(it.function)
            members[it.function.name.lexeme] = it.function.functionBody.type
            symbolTable.defineValue(it.function.name.lexeme, it.function.functionBody.type)
        }

        val instance = Type.InstanceType(
            Expr.Variable(stmt.name),
            params, typeParameters,
            emptyList(), members,
            superclass, superTypeArgs
        )

        val wrapped = wrapGeneric(instance, typeParameters, emptyList())

        symbolTable.defineValue(
            "this",
            wrapped
        )

        symbolTable.endScope()

        symbolTable.defineValue(
            stmt.name.lexeme,
            wrapGeneric(Type.ClassType(instance.className), instance.typeParams, emptyList())
        )

        symbolTable.defineType(
            stmt.name.lexeme,
            wrapped
        )

        stmt.interfaces.forEach {
            val interfaceName = it.first
            val referencedInterface =
                resolveInstanceType(symbolTable.getType(interfaceName) as Type) as Type.InterfaceType

            if (!referencedInterface.canAssignTo(instance)) {
                typeErrors.add(CompileError.TypeError("Cannot assign ${stmt.name.lexeme} to ${interfaceName.lexeme}"))
                val missing = referencedInterface.getMissingMembers(instance)
                missing.entries.forEach { (key, value) ->
                    typeErrors.add(CompileError.TypeError("${stmt.name.lexeme} is missing $key, $value"))
                }
            }
        }
    }


    private fun lookupInitialiserType(type: Type): Type = when (type) {
        is Type.UnresolvedType -> {
            if (!symbolTable.typeExists(type.identifier.name)) resolveInstanceType(type)
            else {
                val resolvedType = lookUpType(type.identifier.name)
                if (resolvedType is Type.InstanceType) {
                    if (type.typeArguments.size == resolvedType.typeParams.size) {
                        val args = mutableMapOf<String, Type>()
                        resolvedType.typeParams.forEachIndexed { index, unresolvedType ->
                            args[unresolvedType.identifier.name.lexeme] =
                                lookupInitialiserType(type.typeArguments[index])
                        }
                        resolvedType.resolveTypeArguments(args)
                    } else resolvedType
                } else if (resolvedType is Type.GenericType) {
                    if (type.typeArguments.size == resolvedType.params.size) {
                        val args = mutableMapOf<String, Type>()
                        resolvedType.params.forEachIndexed { index, unresolvedType ->
                            args[unresolvedType.identifier.name.lexeme] =
                                lookupInitialiserType(type.typeArguments[index])
                        }
                        resolvedType.resolveTypeArguments(args)
                    } else resolvedType
                } else resolvedType
            }
        }
        is Type.InstanceType -> {
            val typeArguments = type.typeArguments.map { lookupInitialiserType(it) }
            type.copy(typeArguments = typeArguments)
        }
        else -> type
    }

    private fun resolveInstanceType(type: Type.UnresolvedType): Type {
        if (!symbolTable.exists(type.identifier.name, type.identifier)) {
            return type
        }
        when (val resolvedType = lookUpType(type.identifier.name)) {
            is Type.InstanceType -> {
                return if (type.typeArguments.size == resolvedType.typeParams.size) {
                    val args = mutableMapOf<String, Type>()
                    resolvedType.typeParams.forEachIndexed { index, unresolvedType ->
                        args[unresolvedType.identifier.name.lexeme] = resolveInstanceType(type.typeArguments[index])
                    }
                    resolvedType.resolveTypeArguments(args)
                } else resolvedType
            }
            is Type.GenericType -> {
                return if (type.typeArguments.size == resolvedType.params.size) {
                    val args = mutableMapOf<String, Type>()
                    resolvedType.params.forEachIndexed { index, unresolvedType ->
                        args[unresolvedType.identifier.name.lexeme] = resolveInstanceType(type.typeArguments[index])
                    }
                    (resolvedType.resolveTypeArguments(args) as Type.GenericType).bodyType
                } else resolvedType
            }
            else -> return resolvedType
        }
    }

    private fun resolveInstanceType(type: Type): Type {
        return when (type) {
            is Type.UnresolvedType -> resolveInstanceType(type)
            is Type.UnionType -> Type.UnionType(type.types.map { resolveInstanceType(it) })
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
            is Type.TupleType -> Type.TupleType(type.types.map { resolveInstanceType(it) })
            is Type.GenericType -> resolveInstanceType(type.bodyType)
            else -> type
        }
    }

    private fun typeCheckLogical(expr: Expr.Logical): Type {
        val left = typeCheck(expr.left)
        val right = typeCheck(expr.right)
        if (!isBooleanType(left) || !isBooleanType(right)) {
            typeErrors.add(CompileError.TypeError("Logical expression should have boolean left and right"))
        }
        val thisType = createBooleanType()
        expr.type = thisType
        return thisType
    }

    private fun typeCheckLiteral(expr: Expr.Literal): Type {
        val type = when (expr.value) {
            is String -> createStringType()
            is Boolean -> createBooleanType()
            is Char -> createCharType()
            else -> when (expr.tokenType) {
                TokenType.INTEGER -> createIntegerType()
                TokenType.DECIMAL -> createDecimalType()
                else -> Type.NullType()
            }
        }
        expr.type = type
        return expr.type
    }

    private fun typeCheckAssign(expr: Expr.Assign): Type {
        val left = symbolTable.lookUpVariableType(expr.name, expr)
        typeCheck(expr.value)
        if (!left.canAssignTo(expr.value.type)) {
            typeErrors.add(CompileError.TypeError("Cannot assign ${expr.value.type} to $left"))
        }
        return expr.value.type
    }

    private fun typeCheckUnary(expr: Expr.Unary): Type {
        val operandType = typeCheck(expr.right)
        return if (operandType is Type.InstanceType) {
            val returnType = getUnaryReturnType(expr, operandType)
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

    private fun typeCheckIfExpr(expr: Expr.If): Type {
        typeCheck(expr.condition)
        if (!isBooleanType(expr.condition.type)) {
            typeErrors.add(CompileError.TypeError("If condition should be boolean"))
        }
        val thenType = typeCheck(expr.thenBranch)
        val elseType = typeCheck(expr.elseBranch)
        val thisType =
            if (thenType::class == elseType::class) thenType else Type.UnionType(listOf(thenType, elseType))
        expr.type = thisType
        return thisType
    }

    private fun typeCheckArray(expr: Expr.Array): Type {
        val elementTypes = expr.values.map { typeCheck(it) }
        val type = flattenTypes(elementTypes)
        val thisType = createArrayType(type)
        expr.type = thisType
        return thisType
    }

    private fun getUnaryReturnType(expr: Expr.Unary, operandType: Type): Type? {
        val unaryMethods = mapOf(
            TokenType.PLUS to "plus",
            TokenType.MINUS to "minus",
            TokenType.BANG to "not",
            TokenType.PLUS_PLUS to "inc",
            TokenType.MINUS_MINUS to "dec"
        )
        val methodName = unaryMethods[expr.operator.type]
        if (methodName != null) {
            val method = operandType.getMemberType(methodName) as Type.FunctionType?
            if (method != null) {
                if (method.params.types.isNotEmpty()) {
                    typeErrors.add(CompileError.TypeError("Unary method should have no parameters"))
                } else {
                    return method.result
                }
            } else {
                typeErrors.add(CompileError.TypeError("Operator ${expr.operator.type} is not overloaded for $operandType"))
            }
        }
        return null
    }

    private fun operatorOperandAllowed(
        operator: TokenType,
        method: Type.FunctionType?,
        right: Type,
    ): Boolean {
        return if (method != null) {
            val paramTypes = method.params.types
            if (paramTypes.size == 1 && paramTypes[0].canAssignTo(right)) {
                true
            } else {
                typeErrors.add(CompileError.TypeError("$method does not accept $right"))
                false
            }
        } else {
            typeErrors.add(CompileError.TypeError("Class does not override $operator operator"))
            false
        }
    }

    private fun typeCheckBinary(expr: Expr.Binary): Type {
        val left = typeCheck(expr.left)
        val right = typeCheck(expr.right)

        if (left is Type.InstanceType) {
            val returnType = getBinaryOperatorType(expr, left, right)
            val resolved = if (returnType is Type.UnresolvedType) {
                resolveInstanceType(returnType)
            } else returnType
            if (resolved != null) {
                expr.type = resolved
                return resolved
            }
        } else {
            typeErrors.add(CompileError.TypeError("Cannot call ${expr.operator.type} on $left"))
        }

        expr.type = Type.NullType()
        return expr.type
    }

    private fun getBinaryOperatorType(expr: Expr.Binary, left: Type.InstanceType, right: Type): Type? {
        if (expr.operator.type in arithmeticOperators) {
            val methodName = operatorMethods[expr.operator.type]!!
            val method = left.getMemberType(methodName) as Type.FunctionType?
            val allowed = operatorOperandAllowed(expr.operator.type, method, right)
            if (allowed) return method?.result!!
        }
        return getComparisonOperatorType(left, expr, right)
    }

    private fun getComparisonOperatorType(
        left: Type.InstanceType,
        expr: Expr.Binary,
        right: Type
    ): Type {
        val methodName = "compareTo"
        if (left.hasMember(methodName)) {
            checkCompare(left, methodName, expr, right)
        } else {
            checkEquals(left, expr, right)
        }
        return createBooleanType()
    }

    private fun checkCompare(
        left: Type.InstanceType,
        methodName: String,
        expr: Expr.Binary,
        right: Type
    ) {
        val method = left.getMemberType(methodName) as Type.FunctionType
        val allowed = operatorOperandAllowed(expr.operator.type, method, right)
        if (allowed) {
            if (!isIntegerType(method.result)) {
                typeErrors.add(CompileError.TypeError("Return type of compare method should be integer"))
            }
        } else {
            if (expr.operator.type !in listOf(
                    TokenType.EQUAL_EQUAL,
                    TokenType.BANG_EQUAL
                )
            ) {
                typeErrors.add(CompileError.TypeError("CompareTo not implemented for $this"))
            }
        }
    }

    private fun checkEquals(left: Type.InstanceType, expr: Expr.Binary, right: Type) {
        val equalMethod = left.getMemberType("equals") as Type.FunctionType?
        val allowed = operatorOperandAllowed(expr.operator.type, equalMethod, right)
        if (allowed) {
            if (!isBooleanType(equalMethod!!.result)) {
                typeErrors.add(CompileError.TypeError("Return type of equals method should be boolean"))
            }
        }
    }

    private fun typeCheckCall(expr: Expr.Call): Type {
        val calleeType = typeCheck(expr.callee)
        expr.arguments.forEach { typeCheck(it) }

        return when (calleeType) {
            is Type.FunctionType -> typeCheckFunctionCall(expr, calleeType)
            is Type.ClassType -> typeCheckInstanceCall(calleeType, expr)
            is Type.GenericType -> typeCheckCall(calleeType, expr)
            else -> calleeType
        }
    }

    private fun typeCheckCall(
        calleeType: Type.GenericType,
        expr: Expr.Call
    ): Type {
        return when (val bodyType = calleeType.bodyType) {
            is Type.FunctionType -> checkGenericFunctionCall(bodyType, expr)
            is Type.ClassType -> checkGenericInstanceCall(bodyType, expr)
            else -> calleeType.bodyType
        }
    }

    private fun checkGenericInstanceCall(
        bodyType: Type.ClassType,
        expr: Expr.Call
    ): Type.InstanceType {
        val classInstance = lookupInstance(bodyType.className.name)
        val typeParams = classInstance.typeParams
        val params = classInstance.params
        val typeArguments = inferTypeArguments(expr.typeArguments, typeParams, params, expr.arguments).map {
            resolveInstanceType(it)
        }

        typeParams.forEachIndexed { index, typeParam ->
            symbolTable.defineType(typeParam.identifier.name.lexeme, typeArguments[index])
        }

        checkGenericCall(expr.arguments, params)

        val args = typeParams.zip(typeArguments).associate {
            Pair(it.first.identifier.name.lexeme, it.second)
        }.toMap()

        val modifiedInstance = classInstance.resolveTypeArguments(args) as Type.InstanceType
        val modifiedParams = modifiedInstance.params
        typeCheckArguments(modifiedParams, expr.arguments)
        expr.type = modifiedInstance

        return modifiedInstance
    }

    private fun checkGenericFunctionCall(
        bodyType: Type.FunctionType,
        expr: Expr.Call
    ): Type {
        val params = (resolveInstanceType(bodyType.params) as Type.TupleType).types
        val typeArguments =
            inferTypeArguments(expr.typeArguments, bodyType.typeParams, params, expr.arguments)
                .map { lookupInitialiserType(it) }

        bodyType.typeParams.forEachIndexed { index, typeParam ->
            symbolTable.defineType(typeParam.identifier.name.lexeme, typeArguments[index])
        }

        checkGenericCall(expr.arguments, params)

        val resultType = typeCheckFunctionCall(expr, bodyType)
        expr.type = resultType
        return expr.type
    }

    private fun typeCheckInstanceCall(calleeType: Type.ClassType, expr: Expr.Call): Type {
        val classInstance = lookUpType(calleeType.className.name) as Type.InstanceType
        val params = classInstance.params
        typeCheckArguments(params, expr.arguments)

        expr.type = classInstance
        return expr.type
    }

    private fun typeCheckFunctionCall(expr: Expr.Call, calleeType: Type.FunctionType): Type {
        val arguments = expr.arguments
        val params = (resolveInstanceType(calleeType.params) as Type.TupleType).types

        typeCheckArguments(params, arguments)

        val resultType = lookupInitialiserType(calleeType.result)

        val thisType = lookupInitialiserType(resultType)
        expr.type = thisType

        return thisType
    }

    private fun typeCheckGet(expr: Expr.Get): Type {
        typeCheck(expr.obj)
        return if (isArrayType(expr.obj.type) && expr.index != null) typeCheckArrayGet(
            expr,
            expr.index
        ) else typeCheckFieldGet(expr)
    }

    private fun typeCheckFieldGet(expr: Expr.Get): Type {
        val calleeType = typeCheck(expr.obj)
        val thisType = calleeType.getMemberType(expr.name.lexeme)
        val resolved = if (thisType is Type.UnresolvedType) resolveInstanceType(thisType)
        else thisType
        expr.type = resolved
        return resolved
    }

    private fun typeCheckArrayGet(
        expr: Expr.Get,
        index: Expr
    ): Type {
        val thisType = (expr.obj.type as Type.InstanceType).typeArguments[0]
        typeCheck(index)
        if (!isIntegerType(index.type)) {
            typeErrors.add(CompileError.TypeError("Array index should be an integer"))
        }

        expr.type = thisType
        return thisType
    }

    private fun typeCheckSet(expr: Expr.Set): Type {
        typeCheck(expr.obj)
        val left = expr.obj.type
        return if (isArrayType(left)) {
            typeCheckArraySet(expr, left)
        } else {
            typeCheckFieldSet(left, expr)
        }
    }

    private fun typeCheckFieldSet(
        left: Type,
        expr: Expr.Set
    ): Type {
        val field = left.getMemberType(expr.name.lexeme)
        typeCheck(expr.value)
        if (!field.canAssignTo(expr.value.type)) {
            typeErrors.add(CompileError.TypeError("Cannot assign ${expr.value.type} to $field"))
        }
        return expr.value.type
    }

    private fun typeCheckArraySet(
        expr: Expr.Set,
        left: Type,
    ): Type {
        typeCheck(expr.value)
        val arrType = (left as Type.InstanceType).typeArguments[0]
        if (expr.index != null) {
            typeCheck(expr.index)
            if (!isIntegerType(expr.index.type)) {
                typeErrors.add(CompileError.TypeError("Array index should be integer"))
            }
        }
        if (!arrType.canAssignTo(expr.value.type)) {
            typeErrors.add(CompileError.TypeError("Cannot assign ${expr.value.type} to $arrType"))
        }
        return expr.value.type
    }

    private fun getMatchType(expr: Expr.Match): Type {
        typeCheck(expr.expr)
        if (!isIntegerType(expr.expr.type) && !isDecimalType(expr.expr.type) && !isStringType(expr.expr.type) && expr.expr.type !is Type.EnumType) {
            typeErrors.add(CompileError.TypeError("Can only use integer, double, enum, or string types in match"))
        }
        val types = mutableListOf<Type>()
        expr.branches.forEach {
            typeCheck(it.first)
            if (it.first.type::class != expr.expr.type::class) {
                typeErrors.add(CompileError.TypeError("Conditions must be same type as match type"))
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
            typeErrors.add(CompileError.TypeError("Can only type match any and union types"))
        }
        val types = mutableListOf<Type>()
        expr.conditions = expr.conditions.map {
            Triple(resolveInstanceType(it.first), it.second, it.third)
        }

        expr.conditions.forEach {
            val block: Expr.Block = makeBlock(it.second)

            symbolTable.beginScope()
            val alias = it.third
            if (alias != null) {
                symbolTable.defineValue(alias.lexeme, it.first)
                symbolTable.defineValue(expr.variable.name.lexeme, variableType)
            } else {
                symbolTable.defineValue(expr.variable.name.lexeme, it.first)
            }

            val returnType = getBlockType(block.statements)
            types.add(returnType)
        }

        if (expr.elseBranch != null) {
            typeCheck(expr.elseBranch)
            types.add(expr.elseBranch.type)
        }

        val hasElse = expr.elseBranch != null
        if (!isTypeMatchExhuastive(
                expr.variable.type,
                expr.conditions.map { resolveInstanceType(it.first) },
                hasElse
            )
        ) {
            typeErrors.add(CompileError.TypeError("Typematch is not exhuastive"))
        }
        expr.type = flattenTypes(types)
        return expr.type
    }

    private fun typeCheckFunction(expr: Expr.Function): Type {
        val block = makeBlock(expr.body)

        val typeParameters = expr.typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) }

        typeParameters.forEach {
            symbolTable.defineType(it.identifier.name.lexeme, it)
        }

        symbolTable.beginScope()

        expr.parameters.forEachIndexed { index, token ->
            val parameterType = (expr.type as Type.FunctionType).params.types[index]
            if (parameterType is Type.InstanceType && expr.typeParameters.any { it.lexeme == parameterType.className.name.lexeme }) {
                symbolTable.defineValue(token.lexeme, Type.InferrableType())
            } else {
                symbolTable.defineValue(token.lexeme, lookupInitialiserType(parameterType))
            }
        }
        val blockReturnType = getBlockType(block.statements)
        val definition = (expr.type as Type.FunctionType)
        val paramTypes = (expr.type as Type.FunctionType).params.types.map { lookupInitialiserType(it) }

        val returnType = if (definition.result is Type.InferrableType) blockReturnType else definition.result

        expr.type =
            wrapGeneric(
                Type.FunctionType(Type.TupleType(paramTypes), typeParameters, returnType),
                typeParameters,
                emptyList()
            )

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
                typeErrors.add(CompileError.TypeError("Number of type params and type arguments don't match"))
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
                var expectedType = lookupInitialiserType(paramType)
                if (expectedType is Type.UnresolvedType) {
                    if (symbolTable.typeExists(expectedType.identifier.name)) {
                        expectedType = lookupInitialiserType(expectedType)
                    }
                }
                if (!arg.type.canAssignTo(expectedType)) {
                    typeErrors.add(CompileError.TypeError("Expected $expectedType and got ${arg.type}"))
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
            if (!param.canAssignTo(argType)) {
                typeErrors.add(CompileError.TypeError("Expected $param and got $argType"))
            }
        }
    }

    private fun isTypeMatchExhuastive(type: Type, branches: List<Type>, hasElse: Boolean): Boolean {
        return if (hasElse) true
        else {
            if (type is Type.AnyType) return false
            if (type is Type.UnionType) type.types.all {
                branches.any { branch ->
                    it.canAssignTo(
                        resolveInstanceType(branch)
                    )
                }
            } else false
        }
    }

    override fun flattenTypes(elementTypes: List<Type>): Type {
        val instanceTypes = elementTypes.filterIsInstance<Type.InstanceType>().toSet().toList()
        val nonInstanceTypes = elementTypes.filter { it !is Type.InstanceType }.distinctBy { it::class }


        val resultTypes = nonInstanceTypes + instanceTypes
        return if (resultTypes.size == 1) resultTypes[0] else Type.UnionType(resultTypes)
    }

    private fun isArrayType(type: Type): Boolean =
        type is Type.InstanceType && type.className.name.lexeme == "Array"

    override fun createArrayType(type: Type): Type {
        val arrayClass = lookupInstance(Expr.Variable(Token(TokenType.IDENTIFIER, "Array", null, -1)).name)
        return arrayClass.resolveTypeArguments(mapOf("T" to type))
    }

    fun createLiteralType(name: String) = lookUpType(Token(TokenType.IDENTIFIER, name, null, -1))

    fun createIntegerType() = createLiteralType("Int")

    fun createDecimalType() = createLiteralType("Decimal")

    fun createStringType() = createLiteralType("String")

    fun createBooleanType() = createLiteralType("Boolean")

    fun createCharType() = createLiteralType("Char")

    fun isIntegerType(type: Type): Boolean = isLiteralType(type, "Int")

    fun isDecimalType(type: Type): Boolean = isLiteralType(type, "Decimal")

    fun isStringType(type: Type): Boolean = isLiteralType(type, "String")

    fun isBooleanType(type: Type): Boolean = isLiteralType(type, "Boolean")

    fun isLiteralType(type: Type, name: String): Boolean = when (type) {
        is Type.InstanceType -> type.className.name.lexeme == name
        is Type.UnresolvedType -> isLiteralType(lookupInitialiserType(type), name)
        else -> false
    }


    override fun lookUpType(name: Token): Type =  symbolTable.getType(name) as Type

    fun getBlockType(statements: List<Stmt>): Type {
        var lastExpr: Type = Type.NullType()

        statements.forEach {
            if (it is Stmt.Expression) {
                lastExpr = typeCheck(it.expression)
            }
            typeCheck(it)
        }
        symbolTable.endScope()

        return lastExpr
    }

    fun wrapGeneric(type: Type, params: List<Type.UnresolvedType>, args: List<Type>): Type {
        return if (params.isEmpty()) type
        else Type.GenericType(params, args, type)
    }
}

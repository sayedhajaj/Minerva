package frontend

import frontend.analysis.TypeChecker

val operatorMethods = mapOf(
    TokenType.PLUS to "add",
    TokenType.MINUS to "subtract",
    TokenType.SLASH to "divide",
    TokenType.STAR to "multiply",
    TokenType.MODULO to "rem"
)

val arithmeticOperators = listOf(
    TokenType.PLUS,
    TokenType.MINUS,
    TokenType.SLASH,
    TokenType.STAR,
    TokenType.MODULO
)

val comparisonOperators = listOf(
    TokenType.LESS,
    TokenType.LESS_EQUAL,
    TokenType.GREATER,
    TokenType.GREATER_EQUAL,
    TokenType.EQUAL_EQUAL,
    TokenType.BANG_EQUAL
)


sealed interface Type {
    fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean

    fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean

    fun getMemberType(member: String, typeChecker: TypeChecker): Type

    class UnionType(val types: List<Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean =
            if (otherType is UnionType)
                otherType.types.all { this.canAssignTo(it, typeChecker) }
            else
                types.any { it.canAssignTo(otherType, typeChecker) }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean =
            types.all { it.hasMemberType(member, otherType, typeChecker) }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = types.joinToString("|")
    }

    data class InstanceType(
        val className: Expr.Variable,
        val params: List<Type>,
        val typeParams: List<UnresolvedType>,
        val typeArguments: List<Type>,
        val members: Map<String, Type>,
        val superclass: InstanceType?,
        val superTypeArgs: List<Type>
    ) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            if (otherType is InstanceType) {
                var otherClass: InstanceType? = otherType
                var matchFound = false
                while (!matchFound && otherClass != null) {
                    if (className.name.lexeme == otherClass.className.name.lexeme) matchFound = true
                    else otherClass = if (otherClass.superclass != null) otherClass.superclass else null
                }
                var allParamsMatch = true
                if (matchFound) {
                    this.typeArguments.forEachIndexed { index, type ->
                        if (otherClass != null && otherClass.typeArguments.size > index) {
                            val otherParam = otherClass.typeArguments[index]
                            if (!type.canAssignTo(otherParam, typeChecker)) {
                                allParamsMatch = false
                            }
                        } else {
                            allParamsMatch = false
                        }
                    }
//                    println(this.typeArguments.size)
//                    println(allParamsMatch)
                    // check if same type parameters?
                }
                return matchFound && allParamsMatch
            } else return false
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean =
            if (members.containsKey(member) && members[member]!!.canAssignTo(type, typeChecker)) {
                true
            } else superclass?.hasMemberType(member, type, typeChecker) == true

        fun hasMember(member: String, typeChecker: TypeChecker): Boolean =
            if (members.containsKey(member)) {
                true
            } else superclass?.hasMember(member, typeChecker) == true

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type =
            if (members.containsKey(member))
                members[member] ?: NullType()
            else
                superclass?.getMemberType(member, typeChecker) ?: NullType()

        fun getUnaryOperatorType(operator: TokenType, typeChecker: TypeChecker): Type? {
            val unaryMethods = mapOf(TokenType.PLUS to "plus", TokenType.MINUS to "minus", TokenType.BANG to "not")
            val methodName = unaryMethods[operator]!!
            val method = getMemberType(methodName, typeChecker) as FunctionType?
            if (method != null) {
                if (method.params.types.isNotEmpty()) {
                    typeChecker.typeErrors.add("Unary method should have no parameters")
                } else {
                    return method.result
                }
            } else {
                typeChecker.typeErrors.add("Operator $operator is not overloaded for $this")
            }
            return null
        }

        fun getBinaryOperatorType(operator: TokenType, right: Type, typeChecker: TypeChecker): Type? =
            if (isArithmeticOperator(operator))
                getArithmeticOperatorType(operator, typeChecker, right)
            else if (isComparisonOperator(operator)) typeCheckComparison(typeChecker, operator, right)
            else null


        private fun typeCheckComparison(
            typeChecker: TypeChecker,
            operator: TokenType,
            right: Type
        ): Type {
            val methodName = "compareTo"
            if (hasMember(methodName, typeChecker)) {

                val compareMethod = getMemberType(methodName, typeChecker) as FunctionType?
                val allowed = operatorOperandAllowed(operator, compareMethod, right, typeChecker)
                if (allowed) {
                    if (!typeChecker.isIntegerType(compareMethod!!.result)) {
                        typeChecker.typeErrors.add("Return type of compare method should be integer")
                    }
                } else {
                    if (operator !in listOf(
                            TokenType.EQUAL_EQUAL,
                            TokenType.BANG_EQUAL
                        )
                    ) {
                        typeChecker.typeErrors.add("CompareTo not implemented for $this")
                    }
                }
            } else {

                typeCheckEqual(operator, typeChecker, right)
            }
            return typeChecker.createBooleanType()
        }

        private fun typeCheckEqual(
            operator: TokenType,
            typeChecker: TypeChecker,
            right: Type,
        ) {

            val equalMethod = getMemberType("equals", typeChecker) as FunctionType?
            val allowed = operatorOperandAllowed(operator, equalMethod, right, typeChecker)
            if (allowed) {
                if (!typeChecker.isBooleanType(equalMethod!!.result)) {
                    typeChecker.typeErrors.add("Return type of equals method should be boolean")
                }
            }
        }

        private fun getArithmeticOperatorType(
            operator: TokenType,
            typeChecker: TypeChecker,
            right: Type
        ): Type? {
            val methodName = operatorMethods[operator]!!
            val method = getMemberType(methodName, typeChecker) as FunctionType?
            val allowed = operatorOperandAllowed(operator, method, right, typeChecker)
            if (allowed) {
                return method!!.result
            }
            return null
        }

        private fun operatorOperandAllowed(
            operator: TokenType,
            method: FunctionType?,
            right: Type,
            typeChecker: TypeChecker
        ): Boolean {
            return if (method != null) {
                val paramTypes = method.params.types
                if (paramTypes.size == 1 && paramTypes[0].canAssignTo(right, typeChecker)) {
                    true
                } else {
                    typeChecker.typeErrors.add("$method does not accept $right")
                    false
                }
            } else {
                typeChecker.typeErrors.add("Class does not override $operator operator")
                false
            }
        }

        private fun isComparisonOperator(operator: TokenType) = operator in comparisonOperators

        private fun isArithmeticOperator(operator: TokenType) = operator in arithmeticOperators

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
                "<" + typeArguments.joinToString(",") + ">"
            return "${className.name.lexeme}$typeArgs"
        }
    }

    data class InterfaceType(val members: Map<String, Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean =
            members.all { otherType.hasMemberType(it.key, it.value, typeChecker) }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean =
            members.containsKey(member) && members[member]!!.canAssignTo(type, typeChecker)

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type =
            if (members.containsKey(member)) {
                members[member] ?: NullType()
            } else NullType()

    }

    class NullType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = otherType is NullType

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "null"
    }


    class AnyType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = true

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "Any"
    }

    class FunctionType(val params: TupleType, val typeParams: List<UnresolvedType>, val result: Type) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is FunctionType) {
                val paramsMatch = params.canAssignTo(otherType.params, typeChecker)
                result.canAssignTo(otherType.result, typeChecker) && paramsMatch
            } else false
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "${params}:${result}"
    }

    class InferrableType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = true
        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false
        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()
    }

    class UnresolvedType(var identifier: Expr.Variable, val typeArguments: List<Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = true

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
                "<" + typeArguments.joinToString(",") + ">"
            return "${identifier.name.lexeme}$typeArgs"
        }
    }


    data class EnumContainer(val name: Token, val members: List<Token>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return members.any { it.lexeme == member }
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return if (hasMemberType(member, AnyType(), typeChecker)) {
                val index = members.indexOfFirst { it.lexeme == member }
                return EnumType(this)
            }
            else NullType()
        }
    }

    class EnumType(val parent: EnumContainer) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker) =
            if (otherType is EnumType)
                (otherType.parent == parent)
            else false

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker) = false


        override fun getMemberType(member: String, typeChecker: TypeChecker) = NullType()


    }

    data class TupleType(val types: List<Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is TupleType) {
                types.size == otherType.types.size &&
                        types.zip(otherType.types).all { it.first.canAssignTo(it.second, typeChecker) }
            } else {
                false
            }
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }


        override fun toString(): String = "(${types.joinToString(",")})"

    }
}
package frontend

sealed interface Type {
    fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean

    fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean

    fun getMemberType(member: String, typeChecker: TypeChecker): Type

    class IntegerType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = otherType is IntegerType

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "Int"
    }

    class DoubleType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = otherType is DoubleType

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "Decimal"
    }

    class StringType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = otherType is StringType

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "String"

    }

    class ArrayType(val type: Type) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean =
            if (otherType is ArrayType) type.canAssignTo(otherType.type, typeChecker) else false

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = when (member) {
            "length" -> IntegerType()
            else -> NullType()
        }

        override fun toString(): String = "${type}[]"
    }

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

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type =
            if (members.containsKey(member))
                members[member] ?: NullType()
            else
                superclass?.getMemberType(member, typeChecker) ?: NullType()

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
                "<" + typeArguments.joinToString(",") + ">"
            return "${className.name.lexeme}$typeArgs"
        }
    }

    class InterfaceType(val members: Map<String, Type>) : Type {
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

    class BooleanType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = otherType is BooleanType

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "Boolean"
    }

    class AnyType : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean = true

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "Any"
    }

    class FunctionType(val params: List<Type>, val typeParams: List<UnresolvedType>, val result: Type) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is FunctionType) {
                val paramsMatch = params.size == otherType.params.size &&
                        params.mapIndexed { index, type -> type.canAssignTo(otherType.params[index], typeChecker) }
                            .all { it }
                result.canAssignTo(otherType.result, typeChecker) && paramsMatch
            } else false
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean = false

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type = NullType()

        override fun toString(): String = "(${params.joinToString(",")}):${result}"
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
}
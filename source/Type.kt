import frontend.Expr

sealed interface Type {
    fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean

    fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean

    fun getMemberType(member: String, typeChecker: TypeChecker): Type

    class IntegerType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is IntegerType
        }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "Int"
        }
    }

    class DoubleType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is DoubleType
        }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "Decimal"
        }
    }

    class StringType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is StringType
        }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "String"
        }

    }

    class ArrayType(val type: Type): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is ArrayType) {
                type.canAssignTo(otherType.type, typeChecker)
            } else {
                false
            }
        }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return when(member) {
                "length" -> IntegerType()
                else -> NullType()
            }
        }

        override fun toString(): String {
            return "${type}[]"
        }
    }

    class UnionType(val types: List<Type>): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is UnionType) {
                otherType.types.all { this.canAssignTo(it, typeChecker) }
            } else {
                types.any { it.canAssignTo(otherType, typeChecker) }
            }
        }

        override fun hasMemberType(member: String, otherType: Type, typeChecker: TypeChecker): Boolean {
            return types.all { it.hasMemberType(member, otherType, typeChecker) }
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return types.joinToString("|")
        }
    }

    class InstanceType(
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
                    if (otherClass != null && className.name.lexeme == otherClass.className.name.lexeme) matchFound = true
                    else {
                        if (otherClass.superclass != null) {
                            otherClass = otherClass.superclass
                        } else otherClass = null
                    }
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

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return if (members.containsKey(member) && members[member]!!.canAssignTo(type, typeChecker)) {
                true
            } else {
                superclass?.hasMemberType(member, type, typeChecker) ?: false
            }
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            if (members.containsKey(member)) {
                return members[member] ?: NullType()
            } else {
                if (superclass != null) {
                    return superclass.getMemberType(member, typeChecker)
                } else return NullType()
            }
        }

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
            "<" + typeArguments.joinToString(",") + ">"
            return "${className.name.lexeme}$typeArgs"
        }
    }

    class InterfaceType(val members: Map<String, Type>): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return members.all {
                otherType.hasMemberType(it.key, it.value, typeChecker)
            }
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return members.containsKey(member) && members[member]!!.canAssignTo(type, typeChecker)
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return if (members.containsKey(member)) {
                members[member] ?: NullType()
            } else NullType()
        }

    }

    class NullType(): Type {

        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is NullType
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "null"
        }
    }

    class BooleanType(): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is BooleanType
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "Boolean"
        }
    }

    class AnyType(): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "Any"
        }
    }

    class FunctionType(val params: List<Type>, val typeParams: List<UnresolvedType>, val result: Type): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            if (otherType is FunctionType) {
                val paramsMatch = params.size == otherType.params.size &&
                        params.mapIndexed { index, type ->  type.canAssignTo(otherType.params[index], typeChecker)}.all { it }
                return result.canAssignTo(otherType.result, typeChecker) && paramsMatch
            } else {
                return false
            }
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            return "(${params.joinToString(",")}):${result}"
        }
    }

    class InferrableType : Type{
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }
    }

    class UnresolvedType(var identifier: Expr.Variable, val typeArguments: List<Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
        }

        override fun hasMemberType(member: String, type: Type, typeChecker: TypeChecker): Boolean {
            return false
        }

        override fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            return NullType()
        }

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
                "<" + typeArguments.joinToString(",") + ">"
            return "${identifier.name.lexeme}$typeArgs"
        }
    }
}
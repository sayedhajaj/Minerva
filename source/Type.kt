import frontend.Expr

sealed interface Type {
    fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean

    class IntegerType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is IntegerType
        }

    }

    class DoubleType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is DoubleType
        }
    }

    class StringType: Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is StringType
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
    }

    class UnionType(val types: List<Type>): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return if (otherType is UnionType) {
                otherType.types.all { this.canAssignTo(it, typeChecker) }
            } else {
                types.any { it.canAssignTo(otherType, typeChecker) }
            }
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
                return matchFound
            } else return false
        }

        fun getMemberType(member: String, typeChecker: TypeChecker): Type {
            if (members.containsKey(member)) {
                return members[member] ?: NullType()
            } else {
                if (superclass != null) {
                    return superclass.getMemberType(member, typeChecker)
                } else return NullType()
            }
        }
    }

    class NullType(): Type {

        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is NullType
        }
    }

    class BooleanType(): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return otherType is BooleanType
        }
    }

    class AnyType(): Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
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
    }

    class InferrableType : Type{
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
        }
    }

    class UnresolvedType(var identifier: Expr.Variable, val typeArguments: List<Type>) : Type {
        override fun canAssignTo(otherType: Type, typeChecker: TypeChecker): Boolean {
            return true
        }
    }
}
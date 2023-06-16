package frontend



sealed interface Type {
    fun canAssignTo(otherType: Type): Boolean

    fun hasMemberType(member: String, type: Type): Boolean

    fun getMemberType(member: String): Type

    fun resolveTypeArguments(args: Map<String, Type>): Type

    class UnionType(val types: List<Type>) : Type {
        override fun canAssignTo(otherType: Type): Boolean =
            if (otherType is UnionType)
                otherType.types.all { this.canAssignTo(it) }
            else
                types.any { it.canAssignTo(otherType) }

        override fun hasMemberType(member: String, otherType: Type): Boolean =
            types.all { it.hasMemberType(member, otherType) }

        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }

        override fun toString(): String = types.joinToString("|")
    }

    data class ClassType(val className: Expr.Variable) : Type {
        override fun canAssignTo(otherType: Type): Boolean {
            return true
        }

        override fun hasMemberType(member: String, type: Type): Boolean {
            return false
        }

        override fun getMemberType(member: String): Type {
            return NullType()
        }

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }

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
        override fun canAssignTo(otherType: Type): Boolean {
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
                            if (!type.canAssignTo(otherParam)) {
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

        override fun hasMemberType(member: String, type: Type): Boolean =
            if (members.containsKey(member) && members[member]!!.canAssignTo(type))
                true
            else superclass?.hasMemberType(member, type) == true

        fun hasMember(member: String): Boolean =
            if (members.containsKey(member)) true
            else superclass?.hasMember(member) == true

        override fun getMemberType(member: String): Type =
            if (members.containsKey(member))
                members[member] ?: NullType()
            else
                superclass?.getMemberType(member) ?: NullType()

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            val superArgs: MutableMap<String, Type> = superclass?.typeParams?.zip(superTypeArgs)?.associate {
                var paramType = it.second
                if (paramType is Type.UnresolvedType) {
                    if (args.containsKey(paramType.identifier.name.lexeme))
                        paramType = args[paramType.identifier.name.lexeme]!!
                }
                Pair(it.first.identifier.name.lexeme, paramType)
            }?.toMutableMap()
                ?: mutableMapOf()

            val typeArguments = typeParams.map {
                args[it.identifier.name.lexeme]
            }.filterNotNull()
            return InstanceType(
                className,
                params.map { it.resolveTypeArguments(args) },
                typeParams, typeArguments,
                members.map {
                    Pair(it.key, it.value.resolveTypeArguments(args))

                }.toMap(),
                if (superclass != null) superclass.resolveTypeArguments(superArgs) as InstanceType else null,
                superTypeArgs.map { it.resolveTypeArguments(args) }
            )
        }


        override fun toString(): String {

            return "${className.name.lexeme}"
        }
    }

    data class InterfaceType(val members: Map<String, Type>) : Type {
        override fun canAssignTo(otherType: Type): Boolean =
            members.all { otherType.hasMemberType(it.key, it.value) }

        override fun hasMemberType(member: String, type: Type): Boolean =
            members.containsKey(member) && members[member]!!.canAssignTo(type)

        override fun getMemberType(member: String): Type =
            if (members.containsKey(member)) {
                members[member] ?: NullType()
            } else NullType()

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            val newMembers = members.entries.associate { Pair(it.key, it.value.resolveTypeArguments(args)) }
            return InterfaceType(newMembers)
        }

    }

    class NullType : Type {
        override fun canAssignTo(otherType: Type): Boolean = otherType is NullType

        override fun hasMemberType(member: String, type: Type): Boolean = false

        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }

        override fun toString(): String = "null"
    }


    class AnyType : Type {
        override fun canAssignTo(otherType: Type): Boolean = true

        override fun hasMemberType(member: String, type: Type): Boolean = false

        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }

        override fun toString(): String = "Any"
    }

    class FunctionType(val params: TupleType, val typeParams: List<UnresolvedType>, val result: Type) : Type {
        override fun canAssignTo(otherType: Type): Boolean {
            return if (otherType is FunctionType) {
                val paramsMatch = params.canAssignTo(otherType.params)
                result.canAssignTo(otherType.result) && paramsMatch
            } else false
        }

        override fun hasMemberType(member: String, type: Type): Boolean = false

        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return FunctionType(
                params.resolveTypeArguments(args) as TupleType,
                typeParams,
                result.resolveTypeArguments(args)
            )
        }

        override fun toString(): String = "${params}:${result}"
    }

    class InferrableType : Type {
        override fun canAssignTo(otherType: Type): Boolean = true
        override fun hasMemberType(member: String, type: Type): Boolean = false
        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }
    }

    class UnresolvedType(var identifier: Expr.Variable, val typeArguments: List<Type>) : Type {
        override fun canAssignTo(otherType: Type): Boolean = true

        override fun hasMemberType(member: String, type: Type): Boolean = false

        override fun getMemberType(member: String): Type = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return if (args.containsKey(identifier.name.lexeme)) args[identifier.name.lexeme] ?: this
            else this
        }

        override fun toString(): String {
            val typeArgs = if (typeArguments.isEmpty()) "" else
                "<" + typeArguments.joinToString(",") + ">"
            return "${identifier.name.lexeme}$typeArgs"
        }
    }


    data class EnumContainer(val name: Token, val members: List<Token>) : Type {
        override fun canAssignTo(otherType: Type): Boolean {
            return false
        }

        override fun hasMemberType(member: String, type: Type): Boolean {
            return members.any { it.lexeme == member }
        }

        override fun getMemberType(member: String): Type {
            return if (hasMemberType(member, AnyType())) {
                val index = members.indexOfFirst { it.lexeme == member }
                return EnumType(this)
            } else NullType()
        }

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }
    }

    class EnumType(val parent: EnumContainer) : Type {
        override fun canAssignTo(otherType: Type) =
            if (otherType is EnumType)
                (otherType.parent == parent)
            else false

        override fun hasMemberType(member: String, type: Type) = false


        override fun getMemberType(member: String) = NullType()
        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }


    }

    data class TupleType(val types: List<Type>) : Type {
        override fun canAssignTo(otherType: Type): Boolean {
            return if (otherType is TupleType) {
                types.size == otherType.types.size &&
                        types.zip(otherType.types).all { it.first.canAssignTo(it.second) }
            } else {
                false
            }
        }

        override fun hasMemberType(member: String, type: Type): Boolean {
            return false
        }

        override fun getMemberType(member: String): Type {
            return NullType()
        }

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return TupleType(types.map { it.resolveTypeArguments(args) })
        }


        override fun toString(): String = "(${types.joinToString(",")})"

    }

    data class ModuleType(val members: Map<String, Type>) : Type {

        override fun canAssignTo(otherType: Type): Boolean {
            return otherType is ModuleType && otherType.members.all { hasMemberType(it.key, it.value) }
        }

        override fun hasMemberType(member: String, type: Type): Boolean {
            return members.containsKey(member)
        }

        override fun getMemberType(member: String): Type {
            return members[member] ?: NullType()
        }

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            return this
        }

    }

    class GenericType(val params: List<UnresolvedType>, val typeArguments: List<Type>, val bodyType: Type) : Type {
        override fun canAssignTo(otherType: Type): Boolean {
            return bodyType.canAssignTo(otherType)
        }

        override fun hasMemberType(member: String, type: Type): Boolean {
            return true
        }

        override fun getMemberType(member: String): Type {
            return bodyType.getMemberType(member)
        }

        override fun resolveTypeArguments(args: Map<String, Type>): Type {
            val newTypeArguments = params.mapNotNull {
                args[it.identifier.name.lexeme]
            }
            return GenericType(params, newTypeArguments, bodyType.resolveTypeArguments(args))
        }

        override fun toString(): String {
            val joinedArgs = typeArguments.joinToString(",")
            val joinedParams = params.joinToString(",")
            val bodyParams = joinedArgs.ifEmpty { joinedParams }

            val typeArgs = if (bodyParams.isEmpty()) "" else
                "<$bodyParams>"
            return "${bodyType}$typeArgs"
        }

    }
}
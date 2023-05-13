package frontend.analysis

import frontend.Expr
import frontend.Token
import frontend.Type

interface ITypeChecker {
    val typeErrors: MutableList<String>
    val locals: MutableMap<Expr, Int>

    fun createArrayType(type: Type): Type

    fun flattenTypes(elementTypes: List<Type>): Type

    fun lookUpType(name: Token): Type

    fun resolveTypeArgument(args: Map<String, Type>, type: Type): Type
}
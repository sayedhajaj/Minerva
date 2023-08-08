package frontend.analysis

import frontend.CompileError
import frontend.Expr
import frontend.Token
import frontend.Type

interface ITypeChecker {
    val typeErrors: MutableList<CompileError.TypeError>
    val locals: MutableMap<Expr, Int>

    fun createArrayType(type: Type): Type

    fun flattenTypes(elementTypes: List<Type>): Type

    fun lookUpType(name: Token): Type

    fun lookupInstance(name: Token): Type.InstanceType
}
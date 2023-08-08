package frontend.analysis

import frontend.Expr
import frontend.Token
import frontend.Type

class SymbolTable(var locals: MutableMap<Expr, Int>) {
    val globals = TypeScope()
    var environments = ArrayDeque<TypeScope>()
    val environment: TypeScope
        get() = environments.last()

    init {
        environments.add(globals)
    }

    fun beginScope() {
        environments.add(TypeScope(environment))
    }

    fun endScope() {
        environments.removeLast()
    }


    fun exists(name: Token, expr: Expr): Boolean {
        val distance = locals[expr]
        return if (distance != null)
            environment.getValueAt(distance, name.lexeme) != null
        else
            globals.getValue(name) != null
    }

    fun typeExists(name: Token): Boolean = getType(name) != null

    fun lookUpVariableType(name: Token, expr: Expr): Type {
        val distance = locals[expr]
        return if (distance != null)
            environment.getValueAt(distance, name.lexeme) as Type
        else
            globals.getValue(name) as Type
    }

    fun defineValue(name: String, value: Type?) = environment.defineValue(name, value)

    fun defineType(name: String, value: Type?) = environment.defineType(name, value)

    fun getValueAt(distance: Int, name: String): Type? = environment.getValueAt(distance, name)

    fun getValue(name: Token) : Type? = environment.getValue(name)

    fun getType(name: Token) : Type? = environment.getType(name)
}
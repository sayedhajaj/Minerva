package frontend.analysis

import frontend.Token
import frontend.Type

class TypeScope {
    private val values = mutableMapOf<String, Type?>()
    private val types = mutableMapOf<String, Type?>()

    val enclosing: TypeScope?

    constructor() {
        enclosing = null
    }

    constructor(enclosing: TypeScope) {
        this.enclosing = enclosing
    }

    fun ancestor(distance: Int): TypeScope? {
        var scope: TypeScope? = this
        for (i in 0 until distance) {
            scope = scope?.enclosing
        }
        return scope
    }

    fun defineValue(name: String, value: Type?) {
        values[name] = value
    }

    fun defineType(name: String, value: Type?) {
        types[name] = value
    }

    fun getValue(name: Token) : Type? {
        if(values.containsKey(name.lexeme)) return values[name.lexeme]
        if (enclosing != null) return enclosing.getValue(name)
        return null
    }

    fun getType(name: Token) : Type? {
        if(types.containsKey(name.lexeme)) return types[name.lexeme]
        if (enclosing != null) return enclosing.getType(name)
        return null
    }

    fun getValueAt(distance: Int, name: String): Type? {
        return ancestor(distance)?.values?.get(name)
    }

}

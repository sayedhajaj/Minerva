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

    fun getValue(name: Token) : Type? {
        if(values.containsKey(name.lexeme)) return values[name.lexeme]
        if (enclosing != null) return enclosing.getValue(name)
        return null
    }

    fun assignValue(name: Token, value: Type?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        enclosing?.assignValue(name, value)
    }

    fun getValueAt(distance: Int, name: String): Any? {
        return ancestor(distance)?.values?.get(name)
    }

    fun assignValueAt(distance: Int, name: Token, value: Type?) {
        ancestor(distance)?.assignValue(name, value)
    }

}

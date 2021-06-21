class Environment {
    private val values = mutableMapOf<String, Any?>()

    val enclosing: Environment?

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Environment) {
        this.enclosing = enclosing
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) : Any? {
        if(values.containsKey(name.lexeme)) return values[name.lexeme]
        if (enclosing != null) return enclosing.get(name)
        return null
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        enclosing?.assign(name, value)
    }
}
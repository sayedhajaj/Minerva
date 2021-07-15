class MinervaInstance(val klass: MinervaClass) {

    val fields = mutableMapOf<String, Any?>()

    override fun toString(): String {
        return "${klass.name} instance"
    }

    init {
        klass.fields.entries.forEach {
            fields[it.key] = it.value

        }
    }

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) return fields[name.lexeme]

        val method = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)

        return null
    }

    fun set(name: Token, value: Any?) {
        if (fields.containsKey(name.lexeme)) fields[name.lexeme] = value
    }
}
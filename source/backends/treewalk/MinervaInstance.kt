package backends.treewalk

import frontend.Token

open class MinervaInstance(val klass: MinervaClass?) {

    val fields = mutableMapOf<String, Any?>()

    override fun toString(): String {
        return "${klass?.name} instance"
    }

    init {
        if (klass != null)
            setUpFields(klass)
    }

    fun setUpFields(currentClass: MinervaClass) {
        if (currentClass.superClass != null) {
            setUpFields(currentClass.superClass)
        }
        currentClass.fields.entries.forEach {
            fields[it.key] = it.value
        }
    }

    open fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) return fields[name.lexeme]

        val method = klass?.findMethod(name.lexeme)
        if (method != null) return method.bind(this)


        return null
    }

    fun set(name: Token, value: Any?) {
        if (fields.containsKey(name.lexeme)) fields[name.lexeme] = value
    }
}
package backends.treewalk

import Environment
import frontend.Stmt
import frontend.Token

open class MinervaInstance(val klass: MinervaClass?, val interpreter: Interpreter) {

    val fields = mutableMapOf<String, Any?>()

    override fun toString(): String {
        return "${klass?.name} instance"
    }

    fun initialise(closure: Environment) {
        if (klass != null) setUpFields(klass, interpreter, closure)
    }

    fun initConstructorFields(constructorFields: Map<String, Any?>) {
        constructorFields.entries.forEach {
            fields[it.key] = it.value
        }
    }

    fun setUpFields(currentClass: MinervaClass, interpreter: Interpreter, closure: Environment) {
        if (currentClass.superClass != null) {
            setUpFields(currentClass.superClass, interpreter, closure)
        }

        val environment = Environment(closure)
        environment.define("this", this)

        currentClass.fields.entries.forEach {
            fields[it.key] = interpreter.executeBlock(listOf(Stmt.Expression(it.value)), environment)
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
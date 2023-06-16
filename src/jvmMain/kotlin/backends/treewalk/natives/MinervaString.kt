package backends.treewalk.natives

import backends.treewalk.*
import frontend.analysis.Environment
import frontend.Expr
import frontend.Token

class MinervaString(val value: String, interpreter: Interpreter) : MinervaInstance(
    MinervaClass(
        "String", null, emptyList(),
        MinervaConstructor(
            emptyMap(), emptyList(),
            Expr.Block(emptyList()),
            Environment()
        ), emptyMap(), emptyMap()
    ), interpreter
) {

    override fun toString(): String {
        return value.toString()
    }

    override fun get(name: Token): Any? {
        return when (name.lexeme) {

            "add" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaString
                    return MinervaString(value + right.value, interpreter)
                }
            }

            "equals" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaString
                    return MinervaBoolean(value == right.value, interpreter)
                }
            }

            else -> null
        }
    }
}
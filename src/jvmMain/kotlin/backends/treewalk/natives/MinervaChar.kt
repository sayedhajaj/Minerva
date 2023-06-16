package backends.treewalk.natives

import backends.treewalk.*
import frontend.analysis.Environment
import frontend.Expr
import frontend.Token

class MinervaChar(val value: Char, interpreter: Interpreter) : MinervaInstance(
    MinervaClass(
        "Char", null, emptyList(),
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

            "equals" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaChar
                    return MinervaBoolean(value == right.value, interpreter)
                }
            }

            else -> null
        }
    }
}
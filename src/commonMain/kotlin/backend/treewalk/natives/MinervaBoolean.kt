package backend.treewalk.natives

import backend.treewalk.*
import frontend.analysis.Environment
import frontend.Expr
import frontend.Token

class MinervaBoolean(val value: Boolean, interpreter: Interpreter) : MinervaInstance(
    MinervaClass(
        "Boolean", null, emptyList(),
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
                    val right = arguments[0] as MinervaBoolean
                    return MinervaBoolean(value == right.value, interpreter)
                }
            }


            "not" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaBoolean(!value, interpreter)
                }
            }

            else -> null
        }
    }
}
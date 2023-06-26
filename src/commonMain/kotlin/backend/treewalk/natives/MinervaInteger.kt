package backend.treewalk.natives

import backend.treewalk.*
import frontend.analysis.Environment
import frontend.Expr
import frontend.Token

class MinervaInteger(val value: Int, interpreter: Interpreter) : MinervaInstance(
    MinervaClass(
        "Int", null, emptyList(),
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
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value + right.value, interpreter)
                }
            }

            "subtract" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value - right.value, interpreter)
                }
            }

            "divide" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value / right.value, interpreter)
                }
            }

            "multiply" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value * right.value, interpreter)
                }
            }

            "rem" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value % right.value, interpreter)
                }
            }

            "compareTo" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaInteger
                    return MinervaInteger(value.compareTo(right.value), interpreter)
                }
            }

            "plus" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaInteger(+value, interpreter)
                }
            }

            "minus" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaInteger(-value, interpreter)
                }
            }

            "inc" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaInteger(value+1, interpreter)
                }
            }

            "dec" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaInteger(value-1, interpreter)
                }
            }

            "toDecimal" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaDecimal(value.toDouble(), interpreter)
                }
            }

            else -> null
        }
    }
}
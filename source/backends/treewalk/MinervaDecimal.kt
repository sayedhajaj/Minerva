package backends.treewalk

import frontend.Environment
import frontend.Expr
import frontend.Token

class MinervaDecimal(val value: Double, interpreter: Interpreter) : MinervaInstance(
    MinervaClass(
        "Decimal", null, emptyList(),
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
                    val right = arguments[0] as MinervaDecimal
                    return MinervaDecimal(value + right.value, interpreter)
                }
            }

            "subtract" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaDecimal
                    return MinervaDecimal(value - right.value, interpreter)
                }
            }

            "divide" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaDecimal
                    return MinervaDecimal(value / right.value, interpreter)
                }
            }

            "multiply" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaDecimal
                    return MinervaDecimal(value * right.value, interpreter)
                }
            }

            "compareTo" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val right = arguments[0] as MinervaDecimal
                    return MinervaInteger(value.compareTo(right.value), interpreter)
                }
            }

            "plus" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaDecimal(+value, interpreter)
                }
            }

            "minus" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    return MinervaDecimal(-value, interpreter)
                }
            }
            else -> null
        }
    }
}
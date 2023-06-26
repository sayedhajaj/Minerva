package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class MinervaMath(interpreter: Interpreter) : MinervaModule(members = mapOf(
    "hypot" to object: MinervaCallable {
        override fun arity(): Int = 2

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val x = (arguments[0] as MinervaDecimal).value
            val y = (arguments[1] as MinervaDecimal).value

            return MinervaDecimal(hypot(x, y), interpreter)
        }
    },

    "toRadians" to object: MinervaCallable {
        override fun arity(): Int = 1

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val angle = (arguments[0] as MinervaDecimal).value

            return MinervaDecimal(angle * PI / 180.0, interpreter)
        }
    },

    "toDegrees" to object: MinervaCallable {
        override fun arity(): Int = 1

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val radians = (arguments[0] as MinervaDecimal).value


            return MinervaDecimal(radians * 180.0 / PI, interpreter)
        }
    },

    "sin" to object: MinervaCallable {
        override fun arity(): Int = 1

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val angle = (arguments[0] as MinervaDecimal).value


            return MinervaDecimal(sin(angle), interpreter)
        }
    },

    "cos" to object: MinervaCallable {
        override fun arity(): Int = 1

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val angle = (arguments[0] as MinervaDecimal).value

            return MinervaDecimal(cos(angle), interpreter)
        }
    },


), interpreter) {

}
package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import kotlin.math.hypot
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


), interpreter) {

}
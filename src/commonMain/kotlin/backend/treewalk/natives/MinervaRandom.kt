package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import kotlin.math.hypot
import kotlin.random.Random

class MinervaRandom(interpreter: Interpreter) : MinervaModule(members = mapOf(

    "nextInt" to object: MinervaCallable {
        override fun arity(): Int = 2

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
            val min = (arguments[0] as MinervaInteger).value
            val max = (arguments[1] as MinervaInteger).value
            return MinervaInteger(Random.nextInt(min, max), interpreter)
        }
    }

), interpreter) {

}
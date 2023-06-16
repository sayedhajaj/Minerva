package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import backend.treewalk.MinervaInstance
import frontend.Token

class MinervaMap(interpreter: Interpreter) : MinervaInstance(null, interpreter) {

    val map =  HashMap<String, Any?>()


    override fun get(name: Token): Any? {
        return when (name.lexeme) {
            "get" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val key = arguments[0] as String
                    return map[key]
                }
            }
            "put" -> object : MinervaCallable {
                override fun arity(): Int = 2

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val key = arguments[0] as String
                    val value = arguments[1]
                    return map.put(key, value)
                }
            }
            else -> null
        }
    }
}
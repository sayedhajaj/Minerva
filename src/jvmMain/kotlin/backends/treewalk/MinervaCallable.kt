package backends.treewalk

import backends.treewalk.Interpreter

interface MinervaCallable {

    fun arity(): Int

    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
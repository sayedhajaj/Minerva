package backends.treewalk.natives

import backends.treewalk.Interpreter
import backends.treewalk.MinervaInstance

class MinervaTuple(val elements: Array<Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {
    override fun toString(): String {
        return elements.contentToString()
    }
}
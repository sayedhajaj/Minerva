package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaInstance

class MinervaTuple(val elements: Array<Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {
    override fun toString(): String {
        return elements.contentToString()
    }
}
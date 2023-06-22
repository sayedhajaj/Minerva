package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaInstance

open class MinervaModule(val members: Map<String, Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {
    init {
        members.forEach { (t, u) ->  fields[t] = u}
    }
}
package backends.treewalk.natives

import backends.treewalk.Interpreter
import backends.treewalk.MinervaInstance

class MinervaModule(val members: Map<String, Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {
    init {
        members.forEach { (t, u) ->  fields[t] = u}
    }
}
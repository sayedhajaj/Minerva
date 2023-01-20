package backends.treewalk

class MinervaTuple(val elements: Array<Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {
    override fun toString(): String {
        return elements.contentToString()
    }
}
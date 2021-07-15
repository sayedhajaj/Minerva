class MinervaClass(
    val name: String,
    val constructor: MinervaConstructor,
    val methods: Map<String, MinervaFunction>,
    val fields: Map<String, Any?>
)  : MinervaCallable {

    override fun arity() = constructor.parameters.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): MinervaInstance {
        val instance = MinervaInstance(this)
        val boundConstructor = constructor.bind(instance)
        boundConstructor.instance = instance
        boundConstructor.call(interpreter, arguments)
        return instance
    }

    override fun toString(): String {
        return name
    }

    fun findMethod(lexeme: String): MinervaFunction? {
        return methods[lexeme]
    }

}
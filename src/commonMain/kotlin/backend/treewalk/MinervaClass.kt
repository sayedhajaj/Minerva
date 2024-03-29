package backend.treewalk

import frontend.Expr
import kotlin.js.JsName

class MinervaClass(
    val name: String,
    val superClass: MinervaClass?,
    val superArguments: List<Expr>,
    @JsName("classConstructor")
    val constructor: MinervaConstructor,
    val methods: Map<String, MinervaFunction>,
    val fields: Map<String, Expr>
)  : MinervaCallable {

    override fun arity() = constructor.parameters.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): MinervaInstance {
        val instance = MinervaInstance(this, interpreter)
        initConstructorFields(interpreter, arguments, instance)
        callConstructor(interpreter, arguments, instance)

        return instance
    }

    fun initConstructorFields(interpreter: Interpreter, arguments: List<Any?>, instance: MinervaInstance) {
        val parentClass = superClass
        if (parentClass != null) {
            val superArgs = superArguments.map {
                if (it is Expr.Variable) {
                    val argumentIndex = constructor.parameters.indexOfFirst { token ->  token.lexeme == it.name.lexeme}
                    if (argumentIndex >= 0) arguments[argumentIndex]
                    else interpreter.evaluate(it)
                } else
                    interpreter.evaluate(it)
            }
            parentClass.initConstructorFields(interpreter, superArgs, instance)
        }
        val fields = constructor.fields.keys.associate {
            constructor.parameters[it].lexeme to arguments[it]
        }

        instance.initConstructorFields(fields)
    }

    fun callConstructor(interpreter: Interpreter, arguments: List<Any?>, instance: MinervaInstance) {
        val parentClass = superClass
        if (parentClass != null) {
            val newConstructor = MinervaConstructor(
                parentClass.constructor.fields,
                parentClass.constructor.parameters,
                parentClass.constructor.body,
                constructor.closure
            )
            val superConstructor = newConstructor.bind(instance)
            superConstructor.instance = instance

            val superArgs = superArguments.map {
                if (it is Expr.Variable) {
                    val argumentIndex = constructor.parameters.indexOfFirst { token ->  token.lexeme == it.name.lexeme}
                    if (argumentIndex >= 0) arguments[argumentIndex]
                    else interpreter.evaluate(it)
                } else
                    interpreter.evaluate(it)
            }

            parentClass.callConstructor(interpreter, superArgs, instance)

        }
        val boundConstructor = constructor.bind(instance)
        boundConstructor.instance = instance

        boundConstructor.call(interpreter, arguments)
        instance.initialise(boundConstructor.closure)
    }

    override fun toString(): String = name


    fun findMethod(lexeme: String): MinervaFunction? = when {
        methods.containsKey(lexeme) -> methods[lexeme]
        superClass != null -> superClass.findMethod(lexeme)
        else -> null
    }

}
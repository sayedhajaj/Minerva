package backends.treewalk

import frontend.analysis.Environment
import frontend.Expr
import frontend.Token

class MinervaConstructor(
    val fields: Map<Int, Token>, val parameters: List<Token>,
    val body: Expr.Block,
    closure: Environment
    ): MinervaFunction(
    "",
    Expr.Function(parameters, emptyList(), body), closure
) {
    var instance: MinervaInstance? = null

    override fun arity(): Int {
        return parameters.size
    }


    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = closure

        for (i in 0 until (parameters.size)) {
            environment.define(parameters[i].lexeme, arguments[i])

        }

        interpreter.executeBlock(body.statements, environment)
        return null
    }

    override fun bind(instance: MinervaInstance): MinervaConstructor {
        val environment = Environment(closure)
        environment.define("this", instance)
        return MinervaConstructor(fields, parameters, body, environment)
    }
}
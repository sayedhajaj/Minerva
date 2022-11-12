package backends.treewalk

import frontend.Environment
import frontend.Expr
import frontend.Stmt

open class MinervaFunction(val name: String, val declaration: Expr.Function, val closure: Environment) :
    MinervaCallable {


    override fun arity() = declaration.parameters.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (i in 0 until (declaration.parameters.size)) {
            environment.define(declaration.parameters[i].lexeme, arguments[i])
        }
        val block = if (declaration.body is Expr.Block)
            declaration.body
        else
            Expr.Block(listOf(Stmt.Expression(declaration.body)))

        return interpreter.executeBlock(block.statements, environment)
    }

    open fun bind(instance: MinervaInstance): MinervaFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return MinervaFunction(name, declaration, environment)
    }

}
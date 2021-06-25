class MinervaFunction(val parameters: List<Token>, val body: Expr, val closure: Environment) : MinervaCallable {


    override fun arity() = parameters.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (i in 0 until (parameters.size)) {
            environment.define(parameters[i].lexeme, arguments[i])
        }
        val block = if (body is Expr.Block)
            body
        else
            Expr.Block(listOf(Stmt.Expression(body)))

        return interpreter.executeBlock(block.statements, environment)
    }
}
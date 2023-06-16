import backends.jvm.BytecodeGenerator

import frontend.*

object Minerva {


    fun run(statements: List<Stmt>) {

        val codeGenerator = BytecodeGenerator(statements)
        val instructions = codeGenerator.generateCode()
        val byteArray = ByteArray(instructions.size)
        for (i in instructions.indices) {
            byteArray[i] = instructions[i]
        }

    }

    fun analyse(syntaxTree: List<Stmt>) {

    }


}

fun main(args: Array<String>) {
    val cli = MinervaCLI()
    cli.processArgs(args)
}
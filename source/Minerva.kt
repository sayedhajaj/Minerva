import Minerva.loadFile
import backends.jvm.BytecodeGenerator
import backends.treewalk.Interpreter
import frontend.Parser
import frontend.Scanner
import frontend.Stmt
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.Charset

object Minerva {


    fun loadFile(filePath: String): String {
        val bytes = Files.readAllBytes(Paths.get(filePath))
        return String(bytes, Charset.defaultCharset())
    }

    fun getSyntaxTree(source: String): List<Stmt> {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        return parser.parse()
    }

    fun run(statements: List<Stmt>) {

        val codeGenerator = BytecodeGenerator(statements)
        val instructions = codeGenerator.generateCode()
        val byteArray = ByteArray(instructions.size)
        for (i in instructions.indices) {
            byteArray[i] = instructions[i]
        }

    }

    fun interpret(syntaxTree: List<Stmt>) {
        val resolver = Resolver()
        resolver.resolve(syntaxTree)
        val interpreter = Interpreter(syntaxTree, resolver.locals)
        interpreter.interpet()
    }
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            Minerva.interpret(Minerva.getSyntaxTree(reader.readLine()))
        }
    } else if(args[0] == "interpret") {
        val source = loadFile(args[1])
        val syntaxTree = Minerva.getSyntaxTree(source)
        Minerva.interpret(syntaxTree)
    }
}
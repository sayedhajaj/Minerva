import Minerva.frontEndPass
import Minerva.loadFile
import backends.jvm.BytecodeGenerator
import backends.treewalk.Interpreter
import frontend.*
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

    fun analyse(syntaxTree: List<Stmt>) {

    }

    fun frontEndPass(source: String): Pair<TypeChecker, List<Stmt>> {
        val resolver = Resolver()
        val syntaxTree = getSyntaxTree(getStandardLibrary() + source)
        resolver.resolve(syntaxTree)
        val typeChecker = TypeChecker(resolver.locals)
        typeChecker.typeCheck(syntaxTree)
        typeChecker.typeErrors.forEach {
            println(it)
        }
        return Pair(typeChecker, syntaxTree)

    }

    fun interpret(typeChecker: TypeChecker, syntaxTree: List<Stmt>): List<String> {
        return if (typeChecker.typeErrors.isEmpty()) {
            val interpreter = Interpreter(syntaxTree, typeChecker.locals, typeChecker)
            interpreter.interpet()
            interpreter.printStatements
        } else {
            emptyList()
        }
    }
}

fun getStandardLibrary() = Minerva::class.java.getResource("standard_library/array.minerva").readText()

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            val (typeChecker, syntaxTree) = frontEndPass(reader.readLine())
            Minerva.interpret(typeChecker, syntaxTree)
        }
    } else if(args[0] == "interpret") {
        val source = loadFile(args[1])
        val (typeChecker, syntaxTree) = frontEndPass(source)
        Minerva.interpret(typeChecker, syntaxTree)
    }
}
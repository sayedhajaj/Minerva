import backends.treewalk.Interpreter
import frontend.*

class MinervaCompiler(val source: String) {

    fun getStandardLibrary() =
        MinervaCompiler::class.java.getResource("standard_library/boolean.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/integer.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/decimal.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/char.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/string.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/array.minerva").readText()


    fun getSyntaxTree(): List<Stmt> {
        val scanner = Scanner(getStandardLibrary()+source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        return parser.parse()
    }

    fun frontEndPass(): Pair<TypeChecker, List<Stmt>> {
        val resolver = Resolver()
        val syntaxTree = getSyntaxTree()
        resolver.resolve(syntaxTree)
        val typeChecker = TypeChecker(resolver.locals)
        typeChecker.typeCheck(syntaxTree)
        typeChecker.typeErrors.forEach {
            println(it)
        }
        return Pair(typeChecker, syntaxTree)

    }

    fun compile() {

    }

    fun interpret(): List<String> {
        val (typeChecker, syntaxTree) = frontEndPass()
        return if (typeChecker.typeErrors.isEmpty()) {
            val interpreter = Interpreter(syntaxTree, typeChecker.locals, typeChecker)
            interpreter.interpet()
            interpreter.printStatements
        } else {
            emptyList()
        }
    }


}
import backends.treewalk.Interpreter
import frontend.*
import frontend.analysis.ITypeChecker
import frontend.analysis.Resolver
import frontend.analysis.TypeChecker
import frontend.parsing.Parser
import frontend.parsing.Scanner

class MinervaCompiler(val source: String) {

    fun getStandardLibrary() =
        MinervaCompiler::class.java.getResource("standard_library/boolean.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/integer.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/decimal.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/char.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/iterable.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/string.minerva").readText() +
                MinervaCompiler::class.java.getResource("standard_library/array.minerva").readText()


    fun getSyntaxTree(): Pair<List<Stmt>, List<CompileError>> {
        val parseErrors = mutableListOf<CompileError>()
        val scanner = Scanner(getStandardLibrary()+source)
        val tokens = scanner.scanTokens()
        parseErrors.addAll(scanner.scannerErrors)
        val parser = Parser(tokens)
        val tree = parser.parse()
        parseErrors.addAll(parser.parseErrors)
        return Pair(tree, parseErrors)
    }

    fun frontEndPass(): Triple<List<CompileError>, ITypeChecker, List<Stmt>> {
        val resolver = Resolver()
        val (syntaxTree, parseErrors) = getSyntaxTree()
        resolver.resolve(syntaxTree)
        val typeChecker = TypeChecker(resolver.locals)
        typeChecker.typeCheck(syntaxTree)
        val compileErrors = parseErrors + typeChecker.typeErrors
        compileErrors.forEach {
            println(it.message)
        }
        return Triple(compileErrors.toList(), typeChecker, syntaxTree)

    }

    fun compile() {

    }

    fun interpret(): List<String> {
        val (compileErrors, typeChecker, syntaxTree) = frontEndPass()
        return if (compileErrors.isEmpty()) {
            val interpreter = Interpreter(syntaxTree, typeChecker.locals, typeChecker)
            interpreter.interpet()
            interpreter.printStatements
        } else {
            emptyList()
        }
    }


}
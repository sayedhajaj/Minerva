import backend.treewalk.Interpreter
import frontend.*
import frontend.analysis.ITypeChecker
import frontend.analysis.Resolver
import frontend.analysis.TypeChecker
import frontend.parsing.Parser
import frontend.parsing.Scanner

class MinervaCompiler(val source: String) {
    val resolver = Resolver()
    val typeChecker = TypeChecker(resolver.locals)
    val interpreter = Interpreter(typeChecker.locals, typeChecker)

    fun defineNative(name: String, value: Any?) {
        interpreter.defineGlobal(name, value)
    }


    fun getSyntaxTree(source: String): Pair<List<Stmt>, List<CompileError>> {
        val parseErrors = mutableListOf<CompileError>()
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        parseErrors.addAll(scanner.scannerErrors)
        val parser = Parser(tokens)
        val tree = parser.parse()
        parseErrors.addAll(parser.parseErrors)
        return Pair(tree, parseErrors)
    }

    fun frontEndPass(): Pair<List<CompileError>, List<Stmt>> {
        val (syntaxTree, parseErrors) = getSyntaxTree(getStandardLibrary() +source)
        resolver.resolve(syntaxTree)
        typeChecker.locals = resolver.locals

        typeChecker.typeCheck(syntaxTree)
        val compileErrors = parseErrors + typeChecker.typeErrors
        compileErrors.forEach {
            println(it.message)
        }
        return Pair(compileErrors.toList(), syntaxTree)

    }

    fun interpret(): List<String> {
        val (compileErrors, syntaxTree) = frontEndPass()
        return if (compileErrors.isEmpty()) {
            interpreter.locals = typeChecker.locals
            interpreter.interpet(syntaxTree)
            interpreter.printStatements
        } else {
            emptyList()
        }
    }
}


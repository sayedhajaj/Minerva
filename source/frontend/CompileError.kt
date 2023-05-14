package frontend

sealed class CompileError(open val message: String) {
    data class ScannerError(val line: Int, override val message: String) : CompileError(message)
    data class ParserError(val token: Token, override val message: String): CompileError(message)
    data class TypeError(override val message: String): CompileError(message)
}

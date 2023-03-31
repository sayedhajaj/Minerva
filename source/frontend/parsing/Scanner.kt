package frontend.parsing

import frontend.Token
import frontend.TokenType
import java.util.HashMap
import kotlin.collections.ArrayList

data class ScannerError(val line: Int, val message: String)

class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = ArrayList()
    val scannerErrors: MutableList<ScannerError> = ArrayList()
    private var start = 0
    private var current = 0
    private var line = 1

    companion object {
        private val keywords: MutableMap<String, TokenType> = HashMap()

        init {
            keywords["class"] = TokenType.CLASS
            keywords["print"] = TokenType.PRINT
            keywords["true"] = TokenType.TRUE
            keywords["false"] = TokenType.FALSE
            keywords["var"] = TokenType.VAR
            keywords["const"] = TokenType.CONST
            keywords["if"] = TokenType.IF
            keywords["else"] = TokenType.ELSE
            keywords["and"] = TokenType.AND
            keywords["or"] = TokenType.OR
            keywords["while"] = TokenType.WHILE
            keywords["for"] = TokenType.FOR
            keywords["foreach"] = TokenType.FOREACH
            keywords["in"] = TokenType.IN
            keywords["until"] = TokenType.UNTIL
            keywords["function"] = TokenType.FUNCTION
            keywords["constructor"] = TokenType.CONSTRUCTOR
            keywords["this"] = TokenType.THIS
            keywords["extends"] = TokenType.EXTENDS
            keywords["super"] = TokenType.SUPER
            keywords["null"] = TokenType.NULL
            keywords["Any"] = TokenType.ANY
            keywords["typematch"] = TokenType.TYPEMATCH
            keywords["match"] = TokenType.MATCH
            keywords["interface"] = TokenType.INTERFACE
            keywords["implements"] = TokenType.IMPLEMENTS
            keywords["external"] = TokenType.EXTERNAL
            keywords["printType"] = TokenType.PRINT_TYPE
            keywords["as"] = TokenType.AS
            keywords["enum"] = TokenType.ENUM
            keywords["type"] = TokenType.TYPE
            keywords["operator"] = TokenType.OPERATOR_MODIFIER
        }
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            '[' -> addToken(TokenType.LEFT_SUB)
            ']' -> addToken(TokenType.RIGHT_SUB)
            '+' -> when {
                match('+') -> addToken(TokenType.PLUS_PLUS)
                match('=') -> addToken(TokenType.PLUS_EQUAL)
                else -> addToken(TokenType.PLUS)
            }
            '-' -> when {
                match('-') -> addToken(TokenType.MINUS_MINUS)
                match('=') -> addToken(TokenType.MINUS_EQUAL)
                else -> addToken(TokenType.MINUS)
            }

            '*' -> addToken(if (match('=')) TokenType.STAR_EQUAL else TokenType.STAR)
            '%' -> addToken(if (match('=')) TokenType.MODULO_EQUAL else TokenType.MODULO)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> when {
                match('=') -> addToken(TokenType.EQUAL_EQUAL)
                match('>') -> addToken(TokenType.ARROW)
                else -> addToken(TokenType.EQUAL)
            }

            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            ';' -> addToken(TokenType.SEMICOLON)
            ':' -> addToken(TokenType.COLON)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '|' -> addToken(TokenType.UNION)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd) advance()
                } else if (match('*')) {
                    while (!(peek() == '*' && peekNext() == '/')) advance()

                    advance()
                    advance()
                } else {
                    addToken(if (match('=')) TokenType.SLASH_EQUAL else TokenType.SLASH)
                }
            }
            '"' -> string()
            '\'' -> char()
            else -> if (isDigit(c)) {
                number()
            } else if (isAlpha(c)) {
                identifier()
            }
        }
    }

    private fun addToken(type: TokenType?, literal: Any? = null) {
        val text = source.substring(start, current)
        type?.let { Token(it, text, literal, line) }?.let { tokens.add(it) }
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' ||
                c in 'A'..'Z' ||
                c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val literal = source.substring(start, current)
        val isKeyword = keywords.containsKey(literal)
        if (isKeyword) {
            addToken(keywords[literal])
        } else {
            addToken(TokenType.IDENTIFIER, literal)
        }
    }

    fun number() {
        while (isDigit(peek())) advance()
        var isDecimal = false
        if (match('.')) {
            isDecimal = true
            while (isDigit(peek())) advance()
        }
        if (isDecimal) {
            addToken(TokenType.DECIMAL, source.substring(start, current).toDouble())
        } else {
            addToken(TokenType.INTEGER, source.substring(start, current).toInt())
        }
    }

    fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd) {
            scannerErrors.add(ScannerError(line, "Unterminated string"))
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    fun char() {
        val char = advance()
        val terminator = advance()

        if (terminator != '\'') {
            scannerErrors.add(ScannerError(line, "Char should only be one character"))
            return
        }

        addToken(TokenType.CHAR, char)
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun peek(): Char = if (isAtEnd) '\u0000' else source[current]

    private fun peekNext(): Char = if (current >= source.length - 1) '\u0000' else source[current + 1]

    private fun match(expected: Char): Boolean {
        if (isAtEnd || source[current] != expected) return false
        current++
        return true
    }

    private val isAtEnd: Boolean
        get() = current >= source.length
}
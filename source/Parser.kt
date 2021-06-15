import java.lang.RuntimeException
import Expr.Binary
import jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType.PRINT
import java.util.ArrayList
import kotlin.math.exp

class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0
    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!isAtEnd) {
            declaration()?.let { statements.add(it) }
        }
        return statements
    }

    private fun declaration(): Stmt? {
        return try {
            if (match(TokenType.CLASS)) classDeclaration() else statement()
        } catch (error: ParseError) {
            null
        }
    }

    private fun classDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")
        while (!isAtEnd && !check(TokenType.RIGHT_BRACE)) advance()
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body")
        advance()
        return Stmt.Class(name)
    }

    private fun statement(): Stmt {
        if (match(TokenType.PRINT)) return printStatement()
        return expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression")
        return Stmt.Expression(expr!!)
    }

    private fun expression(): Expr {
        return term()
    }

    private fun term(): Expr {
        var expr = primary()
        if (match(TokenType.PLUS)) {
            val operator = previous()
            val right = primary()
            expr = Binary(expr!!, operator, right!!)
        }
        return expr
    }

    private fun primary(): Expr {
        if (match(TokenType.NUMBER)) return Expr.Literal(previous().literal)
        return Expr.Literal(null)
    }

    private fun function(): Stmt {
        return functionBody()
    }

    private fun functionBody(): Stmt {
        return Stmt.Block(block())
    }

    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd) {
            declaration()?.let { statements.add(it) }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block")
        return statements
    }

    private fun advance(): Token {
        current++
        return previous()
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private val isAtEnd: Boolean
        private get() = peek().type === TokenType.EOF

    private fun peek() = tokens[current]


    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(tokenType: TokenType): Boolean {
        return if (isAtEnd) false else peek().type === tokenType
    }

    private fun error(token: Token, message: String): ParseError {
        // log here
        return ParseError()
    }
}
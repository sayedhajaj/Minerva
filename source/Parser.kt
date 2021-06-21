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

    private fun declaration(): Stmt {
        if (match(TokenType.VAR)) return varDeclaration()
        return statement()
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

        consume(TokenType.EQUAL, "Expect initialiser")

        val initialiser = expression()

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration")

        return Stmt.Var(name, initialiser)
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
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
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
        return assignment()
    }

    private fun assignment(): Expr {
        var expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal)

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression. ")
            return Expr.Grouping(expr)
        }
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
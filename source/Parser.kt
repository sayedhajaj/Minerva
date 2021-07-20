import java.lang.RuntimeException
import Expr.Binary
import java.util.ArrayList

class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0
    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!isAtEnd) {
            declaration().let { statements.add(it) }
        }
        return statements
    }

    private fun declaration(): Stmt {
        if (match(TokenType.CLASS)) return classDeclaration()
        if (match(TokenType.FUNCTION)) return function()
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

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun classDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")


        var constructorParams = emptyList<Token>()
        var constructorFields = emptyMap<Int, Token>()
        var constructorBody: Expr.Block = Expr.Block(emptyList())

        if (check(TokenType.LEFT_PAREN)) {
            val constructorHeader = constructorParameters()
            constructorParams = constructorHeader.first
            constructorFields = constructorHeader.second
        }

        var superClass: Expr.Variable? = null
        val superArgs = mutableListOf<Expr>()
        if (match(TokenType.EXTENDS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.")
            superClass = Expr.Variable(previous())

            if (match(TokenType.LEFT_PAREN)) {
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        superArgs.add(expression())
                    } while (match(TokenType.COMMA))

                }

                consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
            }

        }


        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Function>()
        val fields = mutableMapOf<Token, Expr>()

        while (!isAtEnd && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.VAR)) {
                // add field
                val name = consume(TokenType.IDENTIFIER, "Expect field name")
                consume(TokenType.EQUAL, "Expect value in field")
                val value = expression()
                fields[name] = value
            } else if (match(TokenType.FUNCTION)) {
                methods.add(function())
            } else if (match(TokenType.CONSTRUCTOR)) {
                consume(TokenType.LEFT_BRACE, "Expect '{' after constructor.")
                constructorBody = Expr.Block(block())

            } else {
                advance()
            }

        }
        val constructor = Stmt.Constructor(constructorFields, constructorParams, superArgs, constructorBody)

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body")

        return Stmt.Class(name, superClass, constructor, methods, fields)
    }

    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.PRINT)) return printStatement()
        if (match(TokenType.WHILE)) return whileStatement()
        return expressionStatement()
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect  '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun ifExpr(): Expr {
        consume(TokenType.LEFT_PAREN, "Expect  '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = expression()

        consume(TokenType.ELSE, "Expect 'else' in if expression.")
        var elseBranch = expression()

        return Expr.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        var expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            } else if (expr is Expr.Get)
                return Expr.Set(expr.obj, expr.name, value)
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
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
        return call()
    }

    private fun constructorParameters(): Pair<MutableList<Token>, MutableMap<Int, Token>> {
        match(TokenType.LEFT_PAREN)
        var isField = false
        val parameters = mutableListOf<Token>()
        val fields = mutableMapOf<Int, Token>()

        var index = 0

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (check(TokenType.VAR)) {
                    advance()
                    isField = true
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"))
                if (isField) fields[index] = previous()
                isField = false
                index++
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")
        return Pair(parameters, fields)
    }

    private fun lambdaExpression(): Expr.Function {
        consume(TokenType.LEFT_PAREN, "Expect ')' after function name.")
        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")
        consume(TokenType.ARROW, "Expect '=> before function body")

        val body = expression()
        return Expr.Function(parameters, body)
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if(match(TokenType.DOT)) {
                val name = consume(TokenType.IDENTIFIER, "Expect property name after '.'")
                expr = Expr.Get(expr, name)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
            } while (match(TokenType.COMMA))

        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, arguments)
    }

    private fun primary(): Expr {
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal)

        if (match(TokenType.SUPER)) {
            val keyword = previous()
            consume(TokenType.DOT, "Expect '.' after 'super'.")
            val method = consume(TokenType.IDENTIFIER, "Expect superclass method name.")
            return Expr.Super(keyword, method)
        }

        if (match(TokenType.THIS)) return  Expr.This(previous())

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression. ")
            return Expr.Grouping(expr)
        }


        if (match(TokenType.LEFT_BRACE)) return Expr.Block(block())

        if (match(TokenType.IF)) return ifExpr()

        if (match(TokenType.FUNCTION)) return lambdaExpression()

        return Expr.Literal(null)
    }

    private fun function(): Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        return Stmt.Function(name, lambdaExpression())
    }


    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd) {
            declaration().let { statements.add(it) }
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
        println(message)
        return ParseError()
    }
}
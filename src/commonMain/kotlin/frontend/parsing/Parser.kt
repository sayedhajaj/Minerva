package frontend.parsing

import frontend.*
import frontend.Expr.Binary


class Parser(private val tokens: List<Token>) {

    val parseErrors: MutableList<CompileError.ParserError> = mutableListOf()

    private var current = 0
    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!isAtEnd()) {
            declaration().let { statements.add(it) }
        }
        return statements
    }

    private fun declaration(): Stmt = when {
        match(TokenType.EXTERNAL) -> externalDeclaration()
        match(TokenType.CLASS) -> classDeclaration()
        match(TokenType.INTERFACE) -> interfaceDeclaration()
        match(TokenType.FUNCTION) -> function()
        match(TokenType.VAR) -> varInitialisation()
        match(TokenType.CONST) -> varInitialisation(true)
        match(TokenType.ENUM) -> enumDeclaration()
        match(TokenType.TYPE) -> typeDeclaration()
        match(TokenType.MODULE) -> moduleDeclaration()
        else -> statement()
    }

    private fun typeDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect Identifier")
        consume(TokenType.EQUAL, "Expect '='")
        val type = typeExpression()
        return Stmt.TypeDeclaration(name, type)
    }

    private fun enumDeclaration(): Stmt.Enum {
        val name = consume(TokenType.IDENTIFIER, "Expect enum name")
        val members = mutableListOf<Token>()

        consume(TokenType.LEFT_BRACE, "Expect '{' before enum body")

        if (!check(TokenType.RIGHT_BRACE)) {
            do {
                members.add(consume(TokenType.IDENTIFIER, "Expect identifier"))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after enum members")

        return Stmt.Enum(name, members)
    }

    private fun moduleDeclaration(): Stmt.Module {
        val name = consume(TokenType.IDENTIFIER, "Expect module name")
        val functions = mutableListOf<Stmt.Function>()
        val fields = mutableListOf<Stmt.Var>()
        val enums = mutableListOf<Stmt.Enum>()
        val modules = mutableListOf<Stmt.Module>()


        consume(TokenType.LEFT_BRACE, "Expect '{' before module body")

        val classes = mutableListOf<Stmt.Class>()

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.CLASS)) classes.add(classDeclaration())
            else if (match(TokenType.FUNCTION)) functions.add(function())
            else if (match(TokenType.ENUM)) enums.add(enumDeclaration())
            else if (match(TokenType.CONST)) fields.add(varInit(true))
            else if (match(TokenType.MODULE)) modules.add(moduleDeclaration())
            else advance()
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after module")

        return Stmt.Module(name, modules, classes, functions, enums, fields)
    }

    private fun externalDeclaration(): Stmt = when {
        match(TokenType.FUNCTION) -> functionDeclaration()
        match(TokenType.CLASS) -> classTypeDeclaration()
        match(TokenType.MODULE) -> externalModule()
        else -> statement()
    }


    private fun varInitialisation(isConst: Boolean = false): Stmt =
        if (match(TokenType.LEFT_PAREN)) destructure() else varInit(isConst)

    private fun varInit(isConst: Boolean): Stmt.Var {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val type: Type = if (match(TokenType.COLON)) typeExpression() else Type.InferrableType()
        consume(TokenType.EQUAL, "Expect initialiser")
        val initialiser = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration")
        return Stmt.Var(name, initialiser, isConst, type)
    }

    private fun destructure(): Stmt.Destructure {
        val variables = mutableListOf<Stmt.VarDeclaration>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                var type: Type = Type.InferrableType()
                val name = consume(TokenType.IDENTIFIER, "Expect identifier")
                if (match(TokenType.COLON)) type = typeExpression()
                variables.add(Stmt.VarDeclaration(name, type))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")
        consume(TokenType.EQUAL, "Expect initialiser")
        val initialiser = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration")
        return Stmt.Destructure(variables, initialiser, initialiser.type)
    }

    private fun varDeclaration(): Stmt.VarDeclaration {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val type: Type = if (match(TokenType.COLON)) typeExpression() else Type.AnyType()
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration")
        return Stmt.VarDeclaration(name, type)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after for.")
        val initialiser = if (match(TokenType.SEMICOLON)) null
        else if (match(TokenType.VAR)) varInitialisation() else expressionStatement()
        val condition = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
        val increment = if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) body = Stmt.Expression(Expr.Block(listOf(body, Stmt.Expression(increment))))

        body = Stmt.While(condition, body)

        if (initialiser != null) body = Stmt.Expression(Expr.Block(listOf(initialiser, body)))
        return body
    }

    private fun forEachStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after foreach.")
        val identifier = consume(TokenType.IDENTIFIER, "Expect identifier")
        consume(TokenType.IN, "Expect 'in' after identifier")
        val iterable = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'")
        val body = statement()
        return Stmt.ForEach(identifier, iterable, body)
    }

    private fun untilStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after until.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(Expr.Unary(Token(TokenType.BANG, "", null, -1), condition, false), body)
    }

    private fun classDeclaration(): Stmt.Class {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")
        val interfaces = mutableListOf<Pair<Token, List<Type>>>()

        val typeParameters = if (match(TokenType.LESS)) {
            genericDeclaration()
        } else emptyList()

        var constructorParams = emptyList<Pair<Token, Type>>()
        var constructorFields = emptyMap<Int, Token>()
        var constructorBody: Expr.Block = Expr.Block(emptyList())

        if (check(TokenType.LEFT_PAREN)) {
            val constructorHeader = constructorParameters()
            constructorParams = constructorHeader.first
            constructorFields = constructorHeader.second
        }

        var superClass: Expr.Variable? = null
        val superArgs = mutableListOf<Expr>()
        var superTypeArgs = mutableListOf<Type>()
        if (match(TokenType.EXTENDS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.")
            superClass = Expr.Variable(previous())

            if (match(TokenType.LESS)) {
                superTypeArgs = genericCall()
            }

            if (match(TokenType.LEFT_PAREN)) {
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        superArgs.add(expression())
                    } while (match(TokenType.COMMA))
                }
                consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
            }
        }

        if (match(TokenType.IMPLEMENTS)) {
            do {
                val interfaceName = consume(TokenType.IDENTIFIER, "Expect interface name.")
                var args = emptyList<Type>()
                if (match(TokenType.LESS)) {
                     args = genericCall()
                }
                interfaces.add(Pair(interfaceName, args))
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Method>()
        val fields = mutableListOf<Stmt.Var>()

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.VAR)) {
                fields.add(varInitialisation() as Stmt.Var)
            } else if (match(TokenType.OPERATOR_MODIFIER)) {
                val isOperator = true
                consume(TokenType.FUNCTION, "Expect function after modifier")
                val method = Stmt.Method(function(), isOperator)
                methods.add(method)
            } else if (match(TokenType.FUNCTION)) {
                methods.add(Stmt.Method(function()))
            } else if (match(TokenType.CONSTRUCTOR)) {
                consume(TokenType.LEFT_BRACE, "Expect '{' after constructor.")
                constructorBody = Expr.Block(block())

            } else {
                advance()
            }

        }
        val constructor = Stmt.Constructor(
            constructorFields,
            constructorParams,
            typeParameters,
            superArgs,
            superTypeArgs,
            constructorBody
        )

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body")

        return Stmt.Class(name, superClass, constructor, methods, fields, interfaces)
    }

    private fun classTypeDeclaration(): Stmt.ClassDeclaration {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")
        val typeParameters = if (match(TokenType.LESS)) {
            genericDeclaration()
        } else emptyList()

        var constructorParams = emptyList<Pair<Token, Type>>()
        var constructorFields = emptyMap<Int, Token>()

        if (check(TokenType.LEFT_PAREN)) {
            val constructorHeader = constructorParameters()
            constructorParams = constructorHeader.first
            constructorFields = constructorHeader.second
        }


        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.FunctionDeclaration>()
        val fields = mutableListOf<Stmt.VarDeclaration>()

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.VAR)) {
                // add field
                fields.add(varDeclaration())
            } else if (match(TokenType.FUNCTION)) {
                methods.add(functionDeclaration())
            } else {
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body")

        val constructor = Stmt.ConstructorDeclaration(constructorFields, constructorParams, typeParameters)

        return Stmt.ClassDeclaration(name, constructor, methods, fields)
    }

    fun externalModule(): Stmt.ModuleDeclaration {
        val name = consume(TokenType.IDENTIFIER, "Expect module name")
        val functions = mutableListOf<Stmt.FunctionDeclaration>()
        val fields = mutableListOf<Stmt.VarDeclaration>()
        val enums = mutableListOf<Stmt.Enum>()
        val modules = mutableListOf<Stmt.ModuleDeclaration>()
        val classes = mutableListOf<Stmt.ClassDeclaration>()


        consume(TokenType.LEFT_BRACE, "Expect '{' before module body")


        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.CLASS)) classes.add(classTypeDeclaration())
            else if (match(TokenType.FUNCTION)) functions.add(functionDeclaration())
            else if (match(TokenType.CONST)) fields.add(varDeclaration())
            else if (match(TokenType.MODULE)) modules.add(externalModule())
            else advance()
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after module")

        return Stmt.ModuleDeclaration(name, modules, classes, functions, enums, fields)
    }

    private fun interfaceDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect interface name.")

        val typeParameters = if (match(TokenType.LESS)) {
            genericDeclaration()
        } else emptyList()


        consume(TokenType.LEFT_BRACE, "Expect '{' before interface body.")

        val methods = mutableListOf<Stmt.FunctionDeclaration>()
        val fields = mutableListOf<Stmt.VarDeclaration>()

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.VAR)) {
                // add field
                fields.add(varDeclaration())
            } else if (match(TokenType.FUNCTION)) {
                methods.add(functionDeclaration())
            } else {
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after interface body")

        return Stmt.Interface(name, methods, fields, typeParameters)
    }


    private fun statement(): Stmt = when {
        match(TokenType.IF) -> ifStatement()
        match(TokenType.PRINT) -> printStatement()
        match(TokenType.PRINT_TYPE) -> printType()
        match(TokenType.WHILE) -> whileStatement()
        match(TokenType.FOR) -> forStatement()
        match(TokenType.FOREACH) -> forEachStatement()
        match(TokenType.UNTIL) -> untilStatement()
        else -> expressionStatement()
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect  '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch: Stmt? = if (match(TokenType.ELSE)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun ifExpr(): Expr {
        consume(TokenType.LEFT_PAREN, "Expect  '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = expression()
        consume(TokenType.ELSE, "Expect 'else' in if expression.")
        val elseBranch = expression()
        return Expr.If(condition, thenBranch, elseBranch)
    }

    private fun typeMatchExpr(): Expr {
        consume(TokenType.LEFT_PAREN, "Expect  '(' after 'typematch'.")
        val variable = Expr.Variable(consume(TokenType.IDENTIFIER, "Expect variable"))
        consume(TokenType.RIGHT_PAREN, "Expect ')' after typematch identifier.")

        consume(TokenType.LEFT_BRACE, "Expect '{' after typematch")

        val branches = mutableListOf<Triple<Type, Expr, Token?>>()

        var elseBranch: Expr? = null

        while (!match(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.ELSE)) {
                consume(TokenType.ARROW, "Expect arrow after else")
                elseBranch = expression()
                consume(TokenType.SEMICOLON, "Expect ';'")
            } else branches.add(typeMatchCondition())
        }

        return Expr.TypeMatch(variable, branches, elseBranch)
    }

    private fun matchExpression(): Expr {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'match'.)")
        val expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after match condition")

        consume(TokenType.LEFT_BRACE, "Expect '{' after match")

        val branches = mutableListOf<Pair<Expr, Expr>>()

        var elseBranch: Expr = Expr.Literal(2, TokenType.INTEGER)
        var hasElse = false

        while (!match(TokenType.RIGHT_BRACE)) {
            if (match(TokenType.ELSE)) {
                hasElse = true
                consume(TokenType.ARROW, "Expect arrow after else")
                elseBranch = expression()
//                consume(TokenType.SEMICOLON, "Expect ';'")
            } else branches.add(matchCondition())
        }



        return Expr.Match(expr, branches, elseBranch)
    }

    private fun typeMatchCondition(): Triple<Type, Expr, Token?> {
        val type = typeExpression()
        val alias = if (match(TokenType.AS)) {
            consume(TokenType.IDENTIFIER, "Expect identifier")
        } else null

        consume(TokenType.ARROW, "Expect arrow after type name")
        val thenBranch = expression()
        consume(TokenType.SEMICOLON, "Expect ';'")
        return Triple(type, thenBranch, alias)
    }

    private fun matchCondition(): Pair<Expr, Expr> {
        val condition = expression()
        consume(TokenType.ARROW, "Expect arrow after type name")
        val thenBranch = expression()
//        consume(TokenType.SEMICOLON, "Expect ';'")
        return Pair(condition, thenBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun printType(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.PrintType(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr = assignment()

    private fun tupleType(): Type.TupleType {
        val paramTypes = mutableListOf<Type>()
        do {
            paramTypes.add(typeExpression())
        } while (match(TokenType.COMMA))
        consume(TokenType.RIGHT_PAREN, "Expect closing ')'")
        return Type.TupleType(paramTypes)
    }

    private fun getType(): Type {
        var type = when {
            match(TokenType.ANY) -> Type.AnyType()
            match(TokenType.NULL) -> Type.NullType()
            match(TokenType.IDENTIFIER) -> {
                val identifier = previous()
                val typeArguments = mutableListOf<Type>()
                if (match(TokenType.LESS)) {
                    do {
                        typeArguments.add(typeExpression())
                    } while (match(TokenType.COMMA))
                    consume(TokenType.GREATER, "Expect closing '>'")
                }
                Type.UnresolvedType(Expr.Variable(identifier), typeArguments)
            }
            match(TokenType.LEFT_PAREN) -> {
                var result: Type = tupleType()
                if (match(TokenType.ARROW)) {
                    result = Type.FunctionType(result as Type.TupleType, emptyList(), typeExpression())
                }
                result
            }
            else -> Type.NullType()
        }

        if (match(TokenType.LEFT_SUB)) {
            consume(TokenType.RIGHT_SUB, "Expect closing ']'")
            type =
                Type.UnresolvedType(Expr.Variable(Token(TokenType.IDENTIFIER, "Array", null, -1)), listOf(type))
        }

        return type
    }

    private fun typeList(): List<Type> {
        val types: MutableList<Type> = mutableListOf()
        do {
            types.add(getType())
        } while (match(TokenType.UNION))
        return types
    }

    private fun typeExpression(): Type {
        val types = typeList()
        return when (types.size) {
            0 -> Type.AnyType()
            1 -> types[0]
            else -> Type.UnionType(types)
        }
    }

    private fun assignment(): Expr {
        val expr = or()

        if (match(
                TokenType.EQUAL,
                TokenType.PLUS_EQUAL,
                TokenType.MINUS_EQUAL,
                TokenType.STAR_EQUAL,
                TokenType.SLASH_EQUAL,
                TokenType.MODULO_EQUAL
            )
        ) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value, equals)
            } else if (expr is Expr.Get)
                return Expr.Set(expr.obj, expr.name, value, expr.index)
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
        while (match(TokenType.MINUS, TokenType.PLUS, TokenType.MODULO)) {
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
        if (match(TokenType.BANG, TokenType.MINUS, TokenType.PLUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right, false)
        }
        return postfix()
    }

    private fun postfix(): Expr {
        if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, distance = 1)) {
            val operator = peek()
            current--
            val left = primary()
            advance()
            return Expr.Unary(operator, left, true)
        }
        return call()
    }

    private fun constructorParameters(): Pair<MutableList<Pair<Token, Type>>, MutableMap<Int, Token>> {
        match(TokenType.LEFT_PAREN)
        var isField = false
        val parameters = mutableListOf<Pair<Token, Type>>()
        val fields = mutableMapOf<Int, Token>()

        var index = 0

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (match(TokenType.VAR)) {
                    isField = true
                }
                val identifier = consume(TokenType.IDENTIFIER, "Expect parameter name")
                var type: Type = Type.AnyType()
                if (match(TokenType.COLON)) type = typeExpression()
                parameters.add(Pair(identifier, type))
                if (isField) fields[index] = previous()
                isField = false
                index++
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")
        return Pair(parameters, fields)
    }

    private fun lambdaExpression(): Expr.Function {
        val typeParameters = if (match(TokenType.LESS)) {
            genericDeclaration()
        } else emptyList()

        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
        val parameters = mutableListOf<Token>()
        var parameterTypes = mutableListOf<Type>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                var parameterType: Type = Type.AnyType()
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"))
                if (match(TokenType.COLON)) parameterType = typeExpression()
                parameterTypes.add(parameterType)
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")

        var returnType: Type = Type.InferrableType()
        if (match(TokenType.COLON)) {
            returnType = typeExpression()
        }

        consume(TokenType.ARROW, "Expect '=> before function body")

        val body = expression()
        var result = Expr.Function(parameters, typeParameters, body)
        result.type = Type.FunctionType(
            Type.TupleType(parameterTypes),
            typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) },
            returnType
        )
        return result
    }

    private fun genericDeclaration(): MutableList<Token> {
        val typeParameters = mutableListOf<Token>()
        if (!check(TokenType.GREATER)) {
            do {
                typeParameters.add(consume(TokenType.IDENTIFIER, "Expect generic parameter name"))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.GREATER, "Expect closing >")
        return typeParameters
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            var typeArguments: List<Type> = emptyList()
            if (check(TokenType.LESS)) {
                val nextCharacterIsType = check(TokenType.IDENTIFIER, 1) ||
                        check(TokenType.NULL, 1)
                if (
                    nextCharacterIsType
                    and (check(TokenType.COMMA, 2) or
                            check(TokenType.GREATER, 2) or
                            check(TokenType.UNION, 2) or check(TokenType.LEFT_SUB)
                            )
                ) {
                    match(TokenType.LESS)
                    typeArguments = genericCall()
                }
            }

            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr, typeArguments)
            } else if (match(TokenType.DOT)) {
                val name = consume(TokenType.IDENTIFIER, "Expect property name after '.'")
                expr = Expr.Get(expr, name, null)
            } else if (match(TokenType.LEFT_SUB)) {
                val index = expression()
                val sub = consume(TokenType.RIGHT_SUB, "Expect closing ']'")
                expr = Expr.Get(expr, sub, index)
            } else {
                break
            }
        }

        return expr
    }

    private fun genericCall(): MutableList<Type> {
        val typeArguments = mutableListOf<Type>()
        if (!check(TokenType.GREATER)) {
            do {
                typeArguments.add(typeExpression())
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.GREATER, "Expect >")
        return typeArguments
    }

    private fun finishCall(callee: Expr, typeArguments: List<Type>): Expr {
        val arguments = mutableListOf<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, arguments, typeArguments)
    }

    private fun primary(): Expr = when {
        match(TokenType.TRUE) -> Expr.Literal(true, TokenType.TRUE)
        match(TokenType.FALSE) -> Expr.Literal(false, TokenType.FALSE)
        match(
            TokenType.DECIMAL,
            TokenType.INTEGER,
            TokenType.STRING,
            TokenType.CHAR
        ) -> {
            Expr.Literal(previous().literal, previous().type)
        }
        match(TokenType.NULL) -> Expr.Literal(null, previous().type)
        match(TokenType.SUPER) -> {
            val keyword = previous()
            consume(TokenType.DOT, "Expect '.' after 'super'.")
            val method = consume(TokenType.IDENTIFIER, "Expect superclass method name.")
            Expr.Super(keyword, method)
        }
        match(TokenType.THIS) -> Expr.This(previous())
        match(TokenType.IDENTIFIER) -> Expr.Variable(previous())
        match(TokenType.LEFT_PAREN) -> {
            val values = mutableListOf<Expr>()
            if (!check(TokenType.RIGHT_SUB)) {
                do {
                    values.add(expression())
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression. ")
            if (values.size == 1) {
                Expr.Grouping(values[0])
            } else {
                Expr.Tuple(values)
            }
        }
        match(TokenType.LEFT_SUB) -> {
            val values = mutableListOf<Expr>()
            if (!check(TokenType.RIGHT_SUB)) {
                do {
                    values.add(expression())
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RIGHT_SUB, "Expect closing ']'")
            Expr.Array(values)
        }
        match(TokenType.LEFT_BRACE) -> Expr.Block(block())
        match(TokenType.IF) -> ifExpr()
        match(TokenType.TYPEMATCH) -> typeMatchExpr()
        match(TokenType.MATCH) -> matchExpression()
        match(TokenType.FUNCTION) -> lambdaExpression()
        else -> Expr.Literal(null, TokenType.NULL)
    }


    private fun function(): Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        return Stmt.Function(name, lambdaExpression())
    }

    private fun functionDeclaration(): Stmt.FunctionDeclaration {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        val typeParameters = if (match(TokenType.LESS)) {
            genericDeclaration()
        } else emptyList()

        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
        val parameters = mutableListOf<Token>()
        val parameterTypes = mutableListOf<Type>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                var parameterType: Type = Type.AnyType()
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"))
                if (match(TokenType.COLON)) parameterType = typeExpression()
                parameterTypes.add(parameterType)
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters")

        var returnType: Type = Type.InferrableType()
        if (match(TokenType.COLON)) {
            returnType = typeExpression()
        }
        return Stmt.FunctionDeclaration(
            name, parameters, typeParameters, Type.FunctionType(
                Type.TupleType(parameterTypes),
                typeParameters.map { Type.UnresolvedType(Expr.Variable(it), emptyList()) },
                returnType
            )
        )
    }


    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration().let { statements.add(it) }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block")
        return statements
    }

    private fun advance(): Token {
        current++
        return previous()
    }

    private fun match(vararg types: TokenType, distance: Int = 0): Boolean {
        for (type in types) {
            if (check(type, distance)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun isAtEnd(distance: Int = 0) = peek(distance).type === TokenType.EOF

    private fun peek() = tokens[current]

    private fun peek(distance: Int) = tokens[current + distance]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (!check(type)) error(peek(), message)

        return advance()
    }

    private fun check(tokenType: TokenType): Boolean = if (isAtEnd()) false else peek().type === tokenType


    private fun check(tokenType: TokenType, distance: Int): Boolean =
        if (isAtEnd(distance)) false else peek(distance).type == tokenType

    private fun error(token: Token, message: String) {
        parseErrors.add(CompileError.ParserError(token, message))
    }
}
package frontend

sealed class Expr(var type: Type) {
    class Array(val values: List<Expr>): Expr(Type.InferrableType())
    class Assign(val name: Token, val value: Expr, val operator: Token) : Expr(Type.NullType())
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr(Type.AnyType())
    class Block(val statements: List<Stmt>) : Expr(Type.AnyType())
    class Call(val callee: Expr, val arguments: List<Expr>, val typeArguments: List<Type>): Expr(Type.AnyType())
    class Grouping(val expr: Expr): Expr(Type.AnyType())
    class Get(val obj: Expr, val name: Token, val index: Expr?): Expr(Type.AnyType())
    class Function(
        val parameters: List<Token>,
        val typeParameters: List<Token>,
        val body: Expr
    )
        : Expr(Type.AnyType())
    class If(val condition: Expr, val thenBranch: Expr, val elseBranch: Expr?): Expr(Type.AnyType())
    class While(val condition: Expr, val body: Expr): Expr(Type.AnyType())
    class Literal(val value: Any?, val tokenType: TokenType) : Expr(Type.AnyType())
    class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr(Type.AnyType())
    class Match(val expr: Expr, val branches: List<Pair<Expr, Expr>>, val elseBranch: Expr): Expr(Type.AnyType())
    class Set(val obj: Expr, val name: Token, val value: Expr, val index: Expr?): Expr(Type.AnyType())
    class Super(val keyword: Token, val method: Token): Expr(Type.AnyType())
    class This(val keyword: Token): Expr(
        Type.InstanceType(
            Variable(keyword),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyMap(),
            null,
            emptyList()
        )
    )
    class TypeMatch(val variable: Variable, var conditions: List<Triple<Type, Expr, Token?>>, val elseBranch: Expr?): Expr(
        Type.AnyType()
    )
    class Unary(val operator: Token, val right: Expr, val postfix: Boolean) : Expr(Type.AnyType())
    class Variable(val name: Token): Expr(Type.AnyType())

    class Tuple(val values: List<Expr>): Expr(Type.TupleType(values.map { it.type }))
}
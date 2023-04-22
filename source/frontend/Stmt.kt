package frontend

sealed class Stmt {
    class Class(
        val name: Token,
        val superclass: Expr.Variable?,
        val constructor: Constructor,
        val methods: List<Method>,
        val fields: List<Var>,
        val interfaces: List<Token>
    ) : Stmt()

    class ClassDeclaration(
        val name: Token,
        val constructor: ConstructorDeclaration,
        val methods: List<FunctionDeclaration>,
        val fields: List<VarDeclaration>
    ) : Stmt()

    class Method(val function: Function, val isOperator: Boolean = false)

    class Interface(
        val name: Token,
        val methods: List<FunctionDeclaration>,
        val fields: List<VarDeclaration>,
        val typeParameters: List<Token>
    ) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()

    class Print(val expression: Expr) : Stmt()

    class PrintType(val expression: Expr) : Stmt()

    class Expression(val expression: Expr) : Stmt()

    class Constructor(
        val fields: Map<Int, Token>,
        val parameters: List<Pair<Token, Type>>,
        val typeParameters: List<Token>,
        val superArgs: List<Expr>,
        val superTypeArgs: List<Type>,
        val constructorBody: Expr.Block
    ) : Stmt()

    class ConstructorDeclaration(
        val fields: Map<Int, Token>,
        val parameters: List<Pair<Token, Type>>,
        val typeParameters: List<Token>,
    ) : Stmt()

    class Function(val name: Token, val functionBody: Expr.Function) : Stmt()
    class FunctionDeclaration(
        val name: Token,
        val parameters: List<Token>,
        val typeParameters: List<Token>,
        var type: Type
    ) : Stmt()

    class Var(val name: Token, val initializer: Expr, val isConst: Boolean = false, var type: Type) : Stmt()
    class VarDeclaration(val name: Token, val type: Type) : Stmt()

    class While(val condition: Expr, val body: Stmt) : Stmt()

    class ForEach(val name: Token, val iterable: Expr, val body: Stmt) : Stmt()


    class Enum(val name: Token, val members: List<Token>) : Stmt()

    class Destructure(val names: List<VarDeclaration>, val initializer: Expr, var type: Type) : Stmt()

    class TypeDeclaration(val name: Token, val type: Type) : Stmt()

    class Module(
        val name: Token,
        val modules: List<Module>,
        val classes: List<Class>,
        val functions: List<Function>,
        val enums: List<Enum>,
        val fields: List<Var>
    ) : Stmt()

    class ModuleDeclaration(
        val name: Token,
        val modules: List<ModuleDeclaration>,
        val classes: List<ClassDeclaration>,
        val functions: List<FunctionDeclaration>,
        val enums: List<Enum>,
        val fields: List<VarDeclaration>
    ) : Stmt()
}
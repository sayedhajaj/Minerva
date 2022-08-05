import kotlin.test.expect

class HelloWorldTest {
    @org.junit.jupiter.api.Test
    internal fun resolvesCorrectly() {
        val source = HelloWorldTest::class.java.getResource("hello_world.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)
        val resolver = Resolver()
        resolver.resolve(syntaxTree)
        val typeChecker = TypeChecker(resolver.locals)
        typeChecker.typeCheck(syntaxTree)
        assert(typeChecker.typeErrors.isEmpty())
    }
}

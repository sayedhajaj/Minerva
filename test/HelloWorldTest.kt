import frontend.Resolver
import frontend.TypeChecker
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class HelloWorldTest {
    @org.junit.jupiter.api.Test
    internal fun resolvesCorrectly() {
        val source = HelloWorldTest::class.java.getResource("examples/hello_world.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)
        val resolver = Resolver()
        resolver.resolve(syntaxTree)
        val typeChecker = TypeChecker(resolver.locals)
        typeChecker.typeCheck(syntaxTree)
        assert(typeChecker.typeErrors.isEmpty())
    }

    @Test
    internal fun printsHelloWorld() {
        val source = HelloWorldTest::class.java.getResource("examples/hello_world.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()

        assertContains(output, "Hello World!")
    }
}

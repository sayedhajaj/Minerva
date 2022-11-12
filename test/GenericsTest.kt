import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class GenericsTest {

    @Test
    internal fun testGenerics() {
        val source = HelloWorldTest::class.java.getResource("examples/generics.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("6", "11", "3", "7", "18", "Container<String>"), output, "")
    }
}
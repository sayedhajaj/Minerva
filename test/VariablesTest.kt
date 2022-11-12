import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class VariablesTest {
    @Test
    internal fun testVariables() {
        val source = HelloWorldTest::class.java.getResource("examples/variables.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("hello", "world", "bar", "4"), output, "")
    }
}
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class VariablesTest {
    @Test
    internal fun testVariables() {
        val source = HelloWorldTest::class.java.getResource("variables.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("hello", "world", "bar", "4"), output, "")
    }
}
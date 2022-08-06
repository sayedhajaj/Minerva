import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class FunctionsTest {
    @Test
    internal fun testFunctions() {
        val source = HelloWorldTest::class.java.getResource("functions.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Hello World!", "8", "9", "120"), output, "")
    }
}
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ArithmeticTest {

    @Test
    internal fun testArithmetic() {
        val source = HelloWorldTest::class.java.getResource("arithmetic.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("2", "3.0", "true", "6", "5"), output, "")
    }
}
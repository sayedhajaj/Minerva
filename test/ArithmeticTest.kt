import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ArithmeticTest {

    @Test
    internal fun testArithmetic() {
        val source = HelloWorldTest::class.java.getResource("examples/arithmetic.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("2", "3.0", "true", "6", "5"), output, "")
    }
}
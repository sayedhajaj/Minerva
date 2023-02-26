import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ArithmeticTest {

    @Test
    internal fun testArithmetic() {
        val source = HelloWorldTest::class.java.getResource("examples/arithmetic.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("2", "3.0", "true", "6", "5", "1"), output, "")
    }
}
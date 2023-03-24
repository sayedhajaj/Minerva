import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class VariablesTest {
    @Test
    internal fun testVariables() {
        val source = HelloWorldTest::class.java.getResource("examples/variables.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("hello", "world", "bar", "4"), output, "")
    }

    @Test
    internal fun testConstants() {
        val source = HelloWorldTest::class.java.getResource("examples/constants.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("hello"), output, "")
    }
}
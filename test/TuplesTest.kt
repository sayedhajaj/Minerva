import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class TuplesTest {
    @Test
    internal fun testTuples() {
        val source = HelloWorldTest::class.java.getResource("examples/tuples.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("[of, 100]", "of"), output, "")
    }
}
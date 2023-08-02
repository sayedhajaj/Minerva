package primitives
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TuplesTest {
    @Test
    internal fun testTuples() {
        val source = HelloWorldTest::class.java.getResource("examples/primitives/tuples.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("[of, 100]", "of"), output, "")
    }
}
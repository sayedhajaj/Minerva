package primitives
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CharTest {
    @Test
    internal fun testStrings() {
        val source = HelloWorldTest::class.java.getResource("examples/primitives/chars.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("true"), output, "")
    }
}
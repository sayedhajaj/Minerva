package primitives
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class StringTest {
    @Test
    internal fun testStrings() {
        val source = HelloWorldTest::class.java.getResource("examples/primitives/strings.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("true", "hello world"), output, "")
    }
}
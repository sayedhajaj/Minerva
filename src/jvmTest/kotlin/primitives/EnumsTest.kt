package primitives
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class EnumsTest {
    @Test
    internal fun testEnums() {
        val source = HelloWorldTest::class.java.getResource("examples/primitives/enums.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("North", "0"), output, "")
    }
}
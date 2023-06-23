
import kotlin.test.Test
import kotlin.test.assertContentEquals

class EnumsTest {
    @Test
    internal fun testEnums() {
        val source = HelloWorldTest::class.java.getResource("examples/enums.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("North", "0"), output, "")
    }
}
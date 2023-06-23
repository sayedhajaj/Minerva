
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ArraysTest {
    @Test
    internal fun testArrays() {
        val source = HelloWorldTest::class.java.getResource("examples/arrays.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("4", "[2, 4, 6, 8]", "24", "10", "6", "3"), output, "")
    }
}
package types
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class GenericsTest {

    @Test
    internal fun testGenerics() {
        val source = HelloWorldTest::class.java.getResource("examples/types/generics.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("6", "11", "3", "7", "18", "Container<String>"), output, "")
    }
}
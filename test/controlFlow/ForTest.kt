package controlFlow

import HelloWorldTest
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ForTest {
    @Test
    internal fun testFor() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/for.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), output, "")
    }
}
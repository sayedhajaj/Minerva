package controlFlow

import HelloWorldTest
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class IteratorTest {
    @Test
    internal fun tesIntRange() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/iteration/IntRange.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("2", "4", "6", "8"), output, "")
    }
}
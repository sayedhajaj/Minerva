package controlFlow

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals


class ForeachTest {
    @Test
    internal fun testForEach() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/iteration/foreach.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("2", "4", "6", "8"), output, "")
    }
}
package controlFlow

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class UntilTest {
    @Test
    internal fun testUntil() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/iteration/until.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), output, "")
    }

    @Test
    internal fun testUntilExpr() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/iteration/until_expr.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("3"), output, "")
    }
}
package controlFlow

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MatchTest {
    @Test
    internal fun testMatch() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/match.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("8"), output, "expect fib of 5 to be 8")
    }
}
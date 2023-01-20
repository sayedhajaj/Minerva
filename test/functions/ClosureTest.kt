package functions

import HelloWorldTest
import Minerva
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ClosureTest {
    @Test
    internal fun testCounter() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/counter.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("1", "2"), output, "")
    }

    @Test
    internal fun testClosesOverFreeVariables() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/closes_over_free_variables.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("global", "global"), output, "")
    }

    @Test
    internal fun testAdder() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/adder.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("8"), output, "")
    }
}